package com.krt.basemodule.connection;

public abstract class PersistentConnectStateChangeListener implements IConnectionListener {

    public abstract void onPreConnect();
    public abstract void onConnectCancel();
    public final void onDisconnect() {}

}
