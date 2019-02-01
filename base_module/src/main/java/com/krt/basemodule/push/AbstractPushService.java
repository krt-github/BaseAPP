package com.krt.basemodule.push;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.CallSuper;

import com.krt.basemodule.connection.PersistentConnectStateChangeListener;
import com.krt.basemodule.debug.Debug;
import com.krt.basemodule.utils.NetUtils;

/**
 * @author KRT
 * 2018/11/26
 */
public abstract class AbstractPushService<T extends AbstractPusher> extends Service {
    private final String TAG = this.getClass().getSimpleName();
    private T mPusher;
    private IOnReceiveInterface mOnReceiveCallback;

    @Override
    public IBinder onBind(Intent intent) {
        printi("----- AbstractPushService onBind -----");
        return new IPushAidlInterface.Stub(){
            public void start(IPCMessage startMsg) {
                AbstractPushService.this.start(startMsg);
            }
            public void close() {
                mPusher.forceClose();
            }
            public boolean isRunning() {
                return mPusher.isRunning();
            }
            public void push(IPCMessage msg) {
                mPusher.push(msg);
            }
            public void registerCallback(IOnReceiveInterface callback) {
                mOnReceiveCallback = callback;
            }
            public void unregisterCallback(IOnReceiveInterface callback) {
                //暂不支持多 callback，后续可用 list 支持
                mOnReceiveCallback = null;
            }
        };
    }

    @Override
    public boolean onUnbind(Intent intent) {
        printi("----- AbstractPushService onUnbind -----");
        return true;
    }

    @Override
    public void onCreate() {
        printi("----- AbstractPushService onCreate -----");
        super.onCreate();
        createConnection();
        registerNetworkReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        printi("----- AbstractPushService onStartCommand -----");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        printi("----- AbstractPushService onDestroy -----");
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    @Override
    public void onRebind(Intent intent) {
        printi("----- AbstractPushService onRebind -----");
        super.onRebind(intent);
    }

    private void createConnection(){
        mPusher = createPusher();
        mPusher.setNetworkStateProvider(this::getIsNetworkAvailable);
        mPusher.setOnConnectChangeListener(new PersistentConnectStateChangeListener() {
            public void onPreConnect() {
                AbstractPushService.this.onPreConnect();
            }
            public void onConnectCancel() {AbstractPushService.this.onConnectCancel();}
            public void onConnectSuccess() {
                AbstractPushService.this.onConnectSuccess();
            }
            public void onReceive(byte[] rawData) {
                handleReceiveData(rawData);
            }
        });
    }

    protected T getPusher(){
        return mPusher;
    }

    protected abstract void start(IPCMessage startMsg);

    protected abstract T createPusher();

    protected abstract String getHost();

    protected abstract int getPort();

    protected void handleReceiveData(byte[] receiveData){
        printi("--- RECEIVE --- " + Thread.currentThread().getName());
        if(null != mOnReceiveCallback){
            try {
                IPCMessage ipcMessage = new IPCMessage();
                ipcMessage.setData(receiveData);
                mOnReceiveCallback.onReceive(ipcMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @CallSuper
    protected void onPreConnect(){
        sendInternalCmd(PushInternalCmd.CMD_PREPARE_CONNECT, PushInternalCmd.VALUE_NONE);
    }
    protected void onConnectSuccess(){}
    protected void onNetworkRegain(){}
    protected void onConnectCancel(){}

    protected void sendInternalCmd(String cmd, String value){
        try {
            mOnReceiveCallback.onReceiveInternalCmd(cmd, value);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void printi(String msg){
        Debug.i(TAG, msg);
    }

    protected void printe(String msg){
        Debug.e(TAG, msg);
    }

    private void registerNetworkReceiver(){
        isNetworkAvailable = NetUtils.isConnected();
        Object connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager instanceof ConnectivityManager){
            mNetworkChangeListener = new NetworkChangeListener();
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            ((ConnectivityManager) connectivityManager)
                    .registerNetworkCallback(builder.build(), mNetworkChangeListener);
        }
    }

    private void unregisterNetworkReceiver(){
        if(null != mNetworkChangeListener){
            Object connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connectivityManager instanceof ConnectivityManager){
                ((ConnectivityManager) connectivityManager).unregisterNetworkCallback(mNetworkChangeListener);
            }
        }
    }

    private boolean getIsNetworkAvailable(){
        return isNetworkAvailable;
    }

    private boolean isNetworkAvailable = false;
    private NetworkChangeListener mNetworkChangeListener;
    private class NetworkChangeListener extends ConnectivityManager.NetworkCallback {
        public void onAvailable(Network network) {
            printe("----network onAvailable------");
            if(!isNetworkAvailable) {//avoid twice callback
                onNetworkRegain();
            }
            isNetworkAvailable = true;
        }
        public void onLost(Network network) {
            printe("----network onLost------");
            isNetworkAvailable = NetUtils.isConnected(); //some bug from system
            printe("----is available: " + isNetworkAvailable);
        }
    }

}
