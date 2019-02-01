package com.krt.basemodule.connection;

import com.krt.basemodule.debug.Debug;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author KRT
 * 2018/11/23
 */
public class PersistentConnection {
    private final AtomicReference<Connection> mAtomicConnection = new AtomicReference<>();
    private String mHost;
    private int mPort;
    private IConnectionListener mConnectionListener;

    public PersistentConnection(){}

    public void connect(String host, int port){
        mHost = host;
        mPort = port;
        establish();
    }

    public void setReceiveListener(PersistentConnectStateChangeListener listener){
        mConnectionListener = getConnectionListener(listener);
        setObserver();
    }

    public boolean send(byte[] rawData){
        if(null != mAtomicConnection.get()) {
            return mAtomicConnection.get().send(rawData);
        }
        return false;
    }

    private void establish(){
        mAtomicConnection.set(new Connection());
        while(true) {
            if (mAtomicConnection.get().connect(mHost, mPort)) {
                mAtomicConnection.get().listen();
                break;
            }
            print("--- Retry establish ---");
        }
    }

    private void setObserver(){
        mAtomicConnection.get().setConnectionListener(mConnectionListener);
    }

    private void restart(){
        print("--- Connection restart ---");
        establish();
        setObserver();
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
                restart();
            }
        };
    }

    private void print(String msg){
//        System.out.println(msg);
        Debug.e("PersistentConnection", msg);
    }

}
