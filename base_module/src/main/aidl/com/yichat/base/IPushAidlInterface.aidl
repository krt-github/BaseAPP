package com.yichat.base;

import com.krt.basemodule.push.IPCMessage;
import com.yichat.base.IOnReceiveInterface;

interface IPushAidlInterface {
    void start(in IPCMessage startMsg);
    void close();
    boolean isRunning();
    void push(in IPCMessage msg);
    void registerCallback(in IOnReceiveInterface callback);
    void unregisterCallback(in IOnReceiveInterface callback);
}
