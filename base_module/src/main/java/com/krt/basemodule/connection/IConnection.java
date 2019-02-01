package com.krt.basemodule.connection;

/**
 * @author KRT
 * 2018/11/23
 */
public interface IConnection {
    int STATE_STOPPED = 1;
    int STATE_STOPPING = 2;
    int STATE_RUNNING = 3;

    boolean connect(String ip, int port);
    void listen();
    boolean disconnect();
    boolean send(byte[] rawData);
    void setConnectionListener(IConnectionListener listener);
    boolean isEOF(byte[] receivedRawData);
}
