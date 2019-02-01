package com.yichat.base;

import com.krt.basemodule.push.IPCMessage;

interface IOnReceiveInterface {
    void onReceiveInternalCmd(String cmd, String value);
    void onReceive(in IPCMessage msg);
}
