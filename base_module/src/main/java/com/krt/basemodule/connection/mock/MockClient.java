package com.krt.basemodule.connection.mock;

import com.krt.basemodule.connection.PersistentConnectStateChangeListener;
import com.krt.basemodule.connection.PersistentConnection2;

/**
 * @author KRT
 * 2018/11/23
 */
public class MockClient {

    public static void main(String[] args){
        new MockClient().run();
    }

    private void run(){
        PersistentConnection2 connection = new PersistentConnection2();
        connection.connect("localhost", 8090);
        print("--- connected ---");
        connection.setOnConnectChangeListener(new PersistentConnectStateChangeListener() {
            public void onPreConnect() {}
            public void onConnectCancel() {}
            public void onConnectSuccess() {}
            public void onReceive(byte[] rawData) {
                print("--- onReceive ---");
            }
        });

        while(true){
            connection.send((System.currentTimeMillis() + "").getBytes());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void print(String msg){
        System.out.println(msg);
    }
}
