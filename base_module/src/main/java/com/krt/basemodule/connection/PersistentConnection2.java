package com.krt.basemodule.connection;

import com.krt.basemodule.debug.Debug;

/**
 * @author KRT
 * 2018/11/27
 */
public class PersistentConnection2 extends Connection {
    private String mHost;
    private int mPort;
    private IEOFListener mEOFListener;
    private INetworkStateProvider mNetworkStateProvider;
    private PersistentConnectStateChangeListener mConnectStateChangeListener;
    private static final int ESTABLISH_INTERVAL = 1000 * 3; //s
    private boolean mEnableAutoRestart = true;
    private volatile boolean isBusy = false;

    public PersistentConnection2(){}

    public boolean connect(String host, int port){
        mHost = host;
        mPort = port;
        establish(); //block until success
        return true;
    }

    public void setOnConnectChangeListener(PersistentConnectStateChangeListener listener){
        mConnectStateChangeListener = listener;
        setConnectionListener(getConnectionListener(listener));
    }

    public void setNetworkStateProvider(INetworkStateProvider provider){
        mNetworkStateProvider = provider;
    }

    public void setEOFListener(IEOFListener listener){
        mEOFListener = listener;
    }

    public boolean isEOF(byte[] receivedRawData) {
        if(null != mEOFListener)
            return mEOFListener.isEOF(receivedRawData);
        return super.isEOF(receivedRawData);
    }

    public void close(){
        print("--->> [ Close ] <<---");
        setBusy(false);
        disconnect();
    }

    public void setEnableAutoRestart(boolean enableRestart){
        print("--->> [ Enable restart : " + enableRestart + " ] <<---");
        mEnableAutoRestart = enableRestart;
    }

    public boolean isEnableAutoRestart(){
        return mEnableAutoRestart;
    }

    public boolean isClosed(){
        return !isRunning();
    }

    private synchronized void setBusy(boolean busy){
        isBusy = busy;
    }

    public synchronized boolean isBusy(){
        return isBusy;
    }

    private void establish(){
        setBusy(true);
        print("------establish onPreConnect()------");
        if(null != mConnectStateChangeListener){
            mConnectStateChangeListener.onPreConnect();
        }

        while(true) {
            long mLastEstablishTime = System.currentTimeMillis();
            if (super.connect(mHost, mPort)) {
                listen(); //A new thread start

                delay(600); //Ensure exe Connection.listen() in new thread
                print("------establish onConnectSuccess()------thread: " + Thread.currentThread().getName());
                if(null != mConnectStateChangeListener){
                    mConnectStateChangeListener.onConnectSuccess();
                }
                break;
            }

            if(isNetworkInvalid()){
                setBusy(false);
                print("------establish onConnectCancel()------");
                if(null != mConnectStateChangeListener){
                    mConnectStateChangeListener.onConnectCancel();
                }
                break;
            }

            long curTime = System.currentTimeMillis();
            long earliestTime = mLastEstablishTime + ESTABLISH_INTERVAL;
            if(curTime + 500 < earliestTime){ //offset 500ms
                delay(earliestTime - curTime);
            }
            print("--- Retry establish ---");
        }
    }

    public void restart(){
        print("--- Connection restart ---");
        establish();
    }

    private boolean isNetworkValid(){
        if(null != mNetworkStateProvider){
            int count = 3;
            boolean valid = mNetworkStateProvider.isNetworkAvailable();
            while(!valid){
                if(--count < 0){
                    break;
                }
//                print("------repeat check : " + count);
                delay(600);
                valid = mNetworkStateProvider.isNetworkAvailable();
            }
            return valid;
        }else{
            print("--------INetworkStateProvider is NULL---------");
        }
        return true;
    }

    private boolean isNetworkInvalid(){
        return !isNetworkValid();
    }

    private void delay(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private IConnectionListener getConnectionListener(PersistentConnectStateChangeListener listener){
        return new IConnectionListener() {
            public void onConnectSuccess() {}
            public void onReceive(byte[] rawData) {
                if(null != listener){
                    listener.onReceive(rawData);
                }
            }
            public void onDisconnect() {
                if(isEnableAutoRestart()) {
                    restart();
                }
            }
        };
    }

    private void print(String msg){
//        System.out.println(msg);
        if(DEBUG) {
            Debug.e("PersistentConnection2", msg);
        }
    }
}
