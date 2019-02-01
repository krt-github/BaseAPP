package com.krt.basemodule.push;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.CallSuper;

import com.krt.basemodule.connection.INetworkStateProvider;
import com.krt.basemodule.connection.PersistentConnectStateChangeListener;
import com.krt.basemodule.connection.PersistentConnection2;
import com.krt.basemodule.debug.Debug;

/**
 * @author KRT
 * 2018/11/23
 */
public abstract class AbstractPusher {
    private static final int MESSAGE_TYPE_NORMAL = 1;
    private static final int MESSAGE_TYPE_HEARTBEAT = 2;
    private static final int MESSAGE_TYPE_START_CONNECT = 3;

    private int mHeartbeatIntervalMs = 0; //ms
    private IMessage mHeartbeatMessage;
    private Handler mHandler;
    private final PersistentConnection2 mConnector = new PersistentConnection2();

    public AbstractPusher() {
        mConnector.setEOFListener(AbstractPusher.this::isEOF);
        new Thread() {
            public void run() {
                Looper.prepare();
                mHandler = new PushHandler(mConnector);
                Looper.loop();
            }
        }.start();
    }

    protected abstract boolean isEOF(byte[] rawData);

    protected abstract IMessage generateHeartbeatMessage();

    public void push(IMessage message){
        push(MESSAGE_TYPE_NORMAL, 0, message);
    }

    public void push(int what, int delayMs, IMessage message){
        if(null == mHandler || mConnector.isClosed()){
            Debug.e("Pusher", "----- Pusher not ready!!! ----- close: " + mConnector.isClosed());
            return;
        }

        Message msg = getHandlerMessage();
        msg.what = what;
        msg.obj = message;

        if(delayMs <= 0) {
            mHandler.sendMessage(msg);
        }else{
            mHandler.sendMessageDelayed(msg, delayMs);
        }
    }

    private Message getHandlerMessage(){
        Message msg = mHandler.obtainMessage();
        if(null == msg){
            msg = new Message();
        }
        return msg;
    }

    public void setOnConnectChangeListener(PersistentConnectStateChangeListener listener){
        mConnector.setOnConnectChangeListener(listener);
    }

    public void setNetworkStateProvider(INetworkStateProvider provider){
        mConnector.setNetworkStateProvider(provider);
    }

    public void setHeartbeatInterval(int intervalSeconds){
        if(mHeartbeatIntervalMs > 0){
            return;
        }

        mHeartbeatIntervalMs = intervalSeconds * 1000;
        if(mHeartbeatIntervalMs <= 0){
            Debug.e("Pusher", "------ Heartbeat interval <= 0 ------");
        }else{
            mHandler.removeMessages(MESSAGE_TYPE_HEARTBEAT);
            sendHeartbeat();
        }
    }

    public void start(String host, int port){
        while(null == mHandler){
            try {
                Thread.sleep(20);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Message startMsg = mHandler.obtainMessage();
        if(null == startMsg){
            startMsg = new Message();
        }
        startMsg.what = MESSAGE_TYPE_START_CONNECT;
        startMsg.obj = host;
        startMsg.arg1 = port;
        mHandler.sendMessage(startMsg);
    }

    public void close(){
        clearPingPongMessage();
        mConnector.close();
    }

    @CallSuper
    public void forceClose(){ //disable auto restart
        setEnableAutoRestart(false);
        close();
    }

    public boolean isRunning(){
        return !mConnector.isClosed();
    }

    public void setEnableAutoRestart(boolean enable){
        mConnector.setEnableAutoRestart(enable);
    }

    public void clearPingPongMessage(){
        mHeartbeatMessage = null;
        mHeartbeatIntervalMs = 0;
        mHandler.removeMessages(MESSAGE_TYPE_HEARTBEAT);
    }

    private IMessage getHeartbeatMessage(){
        if(null == mHeartbeatMessage){
            mHeartbeatMessage = generateHeartbeatMessage();
        }
        return mHeartbeatMessage;
    }

    private void sendHeartbeat(){
        if(!mConnector.isClosed()) {
            //Delay send
            push(MESSAGE_TYPE_HEARTBEAT, mHeartbeatIntervalMs, getHeartbeatMessage());
        }
    }

    private /*static*/ class PushHandler extends Handler {
        private final PersistentConnection2 mConnector;
        PushHandler(PersistentConnection2 connection){
            mConnector = connection;
        }

        public void handleMessage(Message msg) {
            if(msg.obj instanceof IMessage){
                if(!mConnector.send(((IMessage) msg.obj).getData())){
                    retry();
                }
            }
            switch(msg.what){
                case MESSAGE_TYPE_NORMAL:
                    break;
                case MESSAGE_TYPE_HEARTBEAT:
                    Debug.e("Pusher", "- PING -");
                    sendHeartbeat();
                    break;
                case MESSAGE_TYPE_START_CONNECT: startConnect(msg); // block method
                    break;
            }
        }

        private void retry(){
            //TODO
            Debug.e("PushHandler", "-------- TODO need retry -------");
        }

        private void startConnect(Message msg){
            if(msg.obj instanceof String){
                boolean busy = mConnector.isBusy();
                Debug.e("PushHandler", "-------- startConnect  is busy: " + busy);
                if(!busy) {
                    mConnector.connect((String) msg.obj, msg.arg1); // block method
                }
            }
        }
    }
}
