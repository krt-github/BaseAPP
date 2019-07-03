package com.krt.fileuploaddownload.download;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;

import java.util.Locale;

public class DownloadCallback {

    protected void onConfig(DownloadTask.Builder taskBuilder){}

    protected void onStart(DownloadTask task){}

    protected void onConnected(DownloadTask task, int blockCount, long currentOffset, long totalLength) {}

    protected void onProgress(DownloadTask task, String percent){}

    protected void onCompleted(DownloadTask task){}

    protected void onCancel(DownloadTask task){}

    protected void onError(DownloadTask task, Exception e){}

    protected void onWarn(DownloadTask task){}

    protected void onRetry(DownloadTask task, ResumeFailedCause cause){}

    protected void onFinally(DownloadTask task){}

    protected String getPercent(long currentPosition, long totalLength){
        float percent = currentPosition * 100f / totalLength;
        return String.format(Locale.CHINESE, "%.2f", percent);
    }
}
