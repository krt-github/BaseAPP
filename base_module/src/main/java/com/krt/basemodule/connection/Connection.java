package com.krt.basemodule.connection;

import com.krt.basemodule.debug.Debug;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author KRT
 * 2018/11/23
 */
public class Connection implements IConnection, Runnable {
    protected static final boolean DEBUG = true;
    private static final String TAG = "Connection";
    private static final int DEFAULT_RECEIVE_BUFFER_SIZE = 1024 * 5;
    private static final int DEFAULT_CONNECT_TIMEOUT = 1000 * 5;

    private Socket mSocket;
    private final AtomicReference<DataOutputStream> mSend = new AtomicReference<>();
    private final AtomicReference<DataInputStream> mReceive = new AtomicReference<>();
    private final AtomicReference<Integer> mState = new AtomicReference<>(STATE_STOPPED);
    private final byte[] mReceiveBuffer;

    private IConnectionListener mConnectionListener;

    public Connection() {
        this(DEFAULT_RECEIVE_BUFFER_SIZE);
    }

    public Connection(int receiveBufferSize) {
        print("Receive buffer size: " + receiveBufferSize);
        mReceiveBuffer = new byte[receiveBufferSize];
    }

    @Override
    public boolean connect(String host, int port) {
        try {
            mSocket = new Socket();
            mSocket.connect(new InetSocketAddress(host, port), DEFAULT_CONNECT_TIMEOUT);
            mSend.set(new DataOutputStream(mSocket.getOutputStream()));
            mReceive.set(new DataInputStream(mSocket.getInputStream()));

            print("Connect success, host: " + host + " port: " + port);
            if(null != mConnectionListener){
                mConnectionListener.onConnectSuccess();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        print("Connect failed");
        return false;
    }

    @Override
    public void listen() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        print("Listening...      Thread: " + Thread.currentThread().getName());
        if (!mState.compareAndSet(STATE_STOPPED, STATE_RUNNING)) {
            print("Exit with error state");
            return;
        }

        final ByteArrayOutputStream dataBuffer = new ByteArrayOutputStream();
        int readLength = 0;
        try {
            byte[] result = null;
            while (mState.get() == STATE_RUNNING) {
//                while(true){
                    readLength = mReceive.get().read(mReceiveBuffer);
                    if(-1 == readLength) {
                        onReadLengthIsNegative();
//                        break;
                    }

                    dataBuffer.write(mReceiveBuffer, 0, readLength);
                    readLength = 0;
//                    mReceiveBuffer.clear();

                    dataBuffer.flush();
                    result = dataBuffer.toByteArray();
                    print("Receive success: ", dataBuffer);

//                    if(isEOF(result) || readLength >= mReceiveBuffer.length){
//                        break;
//                    }
//                }

                if (null != mConnectionListener && null != result) {
                    try {
                        mConnectionListener.onReceive(result);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                result = null;
                dataBuffer.reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (readLength > 0) {
                    dataBuffer.write(mReceiveBuffer, 0, readLength);
                    dataBuffer.flush();
                    byte[] res = dataBuffer.toByteArray();
                    if (null != mConnectionListener && null != res && res.length > 0) {
                        mConnectionListener.onReceive(res);
                    }
                }
                dataBuffer.reset();
                dataBuffer.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }finally {
            try {
                dataBuffer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        disconnect();
        mState.set(STATE_STOPPED);
        print("Exit");
        if(null != mConnectionListener){
            mConnectionListener.onDisconnect();
        }
    }

    @Override
    public boolean disconnect() {
        if(mState.get() != STATE_RUNNING)
            return false;

        mState.set(STATE_STOPPING);

        try {
            try{mSend.get().close();}catch(Exception e){e.getMessage();}
            try{mReceive.get().close();}catch(Exception e){e.getMessage();}
            if (!mSocket.isClosed()) {
                mSocket.close();
            }
            print("-------- Disconnect success --------");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        print("-------- Disconnect with error --------");
        return false;
    }

    @Override
    public boolean send(byte[] rawData) {
        if(mState.get() == STATE_RUNNING) {
            try {
                if(null == rawData || rawData.length <= 0){
                    print("Send cancel: rawData == null or length <= 0");
                }else {
                    mSend.get().write(rawData);
                    mSend.get().flush();
                    print("Send success: ", rawData);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        print("Send failed, state: " + mState.get());
        return false;
    }

    @Override
    public void setConnectionListener(IConnectionListener listener) {
        mConnectionListener = listener;
    }

    @Override
    public boolean isEOF(byte[] receivedRawData) {
        return false;
    }

    protected void onReadLengthIsNegative() throws Exception{
        throw new IOException("----- Maybe service spontaneous close socket -----");
    }

    public boolean isRunning(){
        return STATE_RUNNING == mState.get();
    }

    private void print(String msg) {
//        System.out.println(msg);
        if(DEBUG) {
            Debug.i(TAG, msg);
        }
    }

    private void print(String prefix, byte[] msg) {
//        System.out.println(prefix + new String(msg));
        if (Debug.isEnableDebug()) {//Avoid unnecessarily new String()
            print(prefix + new String(msg));
        }
    }

    private void print(String prefix, ByteArrayOutputStream buffer) {
//        System.out.println(prefix + new String(buffer.toByteArray()));
        if (Debug.isEnableDebug()) {//Avoid unnecessarily new String()
            print(prefix + " size: " + buffer.size() + " " +new String(buffer.toByteArray()));
        }
    }

}
