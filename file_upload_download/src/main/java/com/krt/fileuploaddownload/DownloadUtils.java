package com.krt.fileuploaddownload;

import android.Manifest;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadUtils {

    public static String[] getPermissions(){
        return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET};
    }

    public static void init(boolean enableLog){
        if(null == mInstance){
            synchronized (DownloadUtils.class){
                if(null == mInstance){
                    mInstance = new DownloadUtils();
                }
            }
        }

        mEnableLog = enableLog;
        if(enableLog){
            Util.enableConsoleLog();
        }
    }

    private static boolean mEnableLog = false;
    private static DownloadUtils mInstance;
    private DownloadUtils(){}

    @RequiresPermission(allOf = {"android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.INTERNET"})
    public List<DownloadTask> downloads(DownloadRequest... requests){
        if(null == requests || requests.length <= 0){
            return null;
        }

        List<DownloadTask> list = new ArrayList<>();
        for(DownloadRequest request : requests){
            list.add(download(request));
        }
        return list;
    }

    @RequiresPermission(allOf = {"android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.INTERNET"})
    public static DownloadTask download(DownloadRequest request){
        if(null == request || null == mInstance){
            return null;
        }

        return mInstance.simpleDownload(request);
    }

    public static boolean pauseTask(DownloadTask task){
        if(null != task) {
            return OkDownload.with().downloadDispatcher().cancel(task);
        }
        return false;
    }

    public static void resumeTask(DownloadTask task, boolean isSync, DownloadCallback callback){
        if(null != task && null != callback && null != mInstance) {
            mInstance.doDownload(task, callback, isSync);
        }
    }

    public static boolean cancelTask(DownloadTask task){
        boolean res = false;
        if(null != task) {
            res = pauseTask(task);
            File file = task.getFile();
            if(null != file){
                return file.delete() || res;
            }
        }
        return res;
    }

    public static void unregisterCallback(DownloadCallback... callbacks){
        if(null == callbacks || callbacks.length <= 0){
            return;
        }
        for(DownloadCallback callback : callbacks){
            DownloadCallbackWrapper.unregisterCallback(callback);
        }
    }

    private DownloadTask.Builder createDownloadTaskBuilder(DownloadRequest request){
        return new DownloadTask.Builder(request.downloadUrl, request.saveDir, request.fileName)
                .setMinIntervalMillisCallbackProcess(1000);
    }

    private DownloadTask simpleDownload(DownloadRequest request){
        DownloadTask.Builder taskBuilder = createDownloadTaskBuilder(request);
        request.callback.onConfig(taskBuilder);
        DownloadTask downloadTask = taskBuilder.build();
        doDownload(downloadTask, request.callback, request.syncRequest);
        return downloadTask;
    }

    private void doDownload(DownloadTask task, DownloadCallback callback, boolean isSync){
        if(null == task || null == callback){
            return;
        }

        DownloadCallbackWrapper callbackWrapper = DownloadCallbackWrapper.getCallbackWrapper(task, callback);
        //TODO
        final boolean isNotExistTask = "isNewTask".equals(task.getTag(1000));
        if(isNotExistTask) {
            if (isSync) {
                task.execute(callbackWrapper);
            } else {
                task.enqueue(callbackWrapper);
            }
        }

        printi(isNotExistTask ? "Create new task." : "Exist task, skip.");
    }

    static boolean isEnableLog(){
        return mEnableLog;
    }

    static void printi(String s){
        if(mEnableLog) {
            Log.i("DownloadUtils", s);
        }
    }

    static void printe(String s){
        if(mEnableLog) {
            Log.e("DownloadUtils", s);
        }
    }

}
