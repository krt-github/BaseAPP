package com.krt.fileuploaddownload.upload;

import com.krt.fileuploaddownload.upload.progress.ProgressListener;

import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

public abstract class AbsUploadCallback extends ProgressListener implements Callback {
    public String taskId = "";

    protected void onConfig(Request.Builder requestBuilder){}

    @Override
    public void onProgressChanged(long numBytes, long totalBytes, float percent, float speed) {}

    protected void onCancel(Call call){}

    protected void onFinally(){}

    protected String getPercent(long currentPosition, long totalLength){
        float percent = currentPosition * 100f / totalLength;
        return String.format(Locale.CHINESE, "%.2f", percent);
    }

}
