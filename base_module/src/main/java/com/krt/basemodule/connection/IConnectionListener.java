package com.krt.basemodule.connection;

/**
 * @author KRT
 * 2018/11/23
 */
public interface IConnectionListener {
    void onConnectSuccess();
    void onReceive(byte[] rawData);
    void onDisconnect();
}
