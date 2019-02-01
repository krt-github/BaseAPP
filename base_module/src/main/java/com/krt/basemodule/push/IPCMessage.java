package com.krt.basemodule.push;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author KRT
 * 2018/11/27
 */
public class IPCMessage implements Parcelable, IMessage {
    private byte[] data;

    public IPCMessage() {}

    protected IPCMessage(Parcel in) {
        data = in.createByteArray();
    }

    public static final Creator<IPCMessage> CREATOR = new Creator<IPCMessage>() {
        @Override
        public IPCMessage createFromParcel(Parcel in) {
            return new IPCMessage(in);
        }

        @Override
        public IPCMessage[] newArray(int size) {
            return new IPCMessage[size];
        }
    };

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
        if(null == this.data)
            this.data = new byte[0];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(data);
    }

}
