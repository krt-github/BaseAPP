package com.krt.basemodule.bean;

public class PressRecordResult {
    public static final int ERROR_NO_ERROR = 0;
    public static final int ERROR_UNKNOWN = 1;
    public static final int ERROR_PERMISSSION = 2;
    public static final int ERROR_TOO_SHORT = 3;
    public static final int ERROR_CANCEL = 4;
    public static final int ERROR_MEDIA_UNMOUNT = 5;
    public static final int ERROR_FILE_CREATE_FAILED = 6;
    public static final int ERROR_RECORDER_ERROR = 7;

    public boolean success;
    public int errorCode;
    public String recordFilePath;
    public int duration;

    public PressRecordResult(){}

    public PressRecordResult(boolean success, int errorCode){
        this.success = success;
        this.errorCode = errorCode;
    }

    public PressRecordResult(boolean success, int errorCode, String recordFilePath){
        this.success = success;
        this.errorCode = errorCode;
        this.recordFilePath = recordFilePath;
    }
}
