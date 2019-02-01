package com.krt.basemodule.connection.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author KRT
 * 2018/11/23
 */
public class MockServer {

    public static void main(String[] args){
        new MockServer().run();
    }

    private void run(){
        ServerSocket server = null;
        InputStream is = null;
        OutputStream os = null;
        try {
            print("Server start");
            server = new ServerSocket(8090);
            print("Server host: " + server.getLocalSocketAddress());

            Socket serverSocket = server.accept();
            is = serverSocket.getInputStream();
            os = serverSocket.getOutputStream();
            byte[] buf = new byte[1024 * 5];
            int readLength;
            String msg;
            while(true) {
                if (-1 != (readLength = is.read(buf))) {
                    msg = new String(buf, 0, readLength);
                    print(serverSocket.getLocalSocketAddress() + ": " + msg);
                    os.write(("Reply [" + getTimeString(msg) + "] ").getBytes());
                    os.flush();
                    if("Exit".equals(msg)){
                        print("Server exit normal");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                server.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        print("Server exit");
    }

    private void print(String msg){
        System.out.println(msg);
    }

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private String getTimeString(String timeLong){
        try {
            return sdf.format(new Date(Long.parseLong(timeLong)));
        }catch(Exception e){
            e.printStackTrace();
        }
        return "Error";
    }

}
