package com.krt.fileuploaddownload.download;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener3;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;

import java.util.ArrayList;
import java.util.List;

class DownloadCallbackWrapper extends DownloadListener3 {
    private static SparseArray<DownloadCallbackWrapper> mTaskList = new SparseArray<>();

    static DownloadCallbackWrapper getCallbackWrapper(DownloadTask task, DownloadCallback callback){
        DownloadCallbackWrapper callbackWrapper;
        DownloadTask taskIsExist = OkDownload.with().downloadDispatcher().findSameTask(task);
        if(null != taskIsExist) {
            print("Task is exist, id: " + task.getId());
            callbackWrapper = mTaskList.get(task.getId());
            if (null != callbackWrapper) {
                callbackWrapper.addCallback(callback);
                return callbackWrapper;
            }
        }
        print("Task is not exist.");
        callbackWrapper = new DownloadCallbackWrapper(callback);
        mTaskList.put(task.getId(), callbackWrapper);
        //TODO
        task.addTag(1000, "isNewTask");
        return callbackWrapper;
    }

    static void unregisterCallback(DownloadCallback callback){
        int size = mTaskList.size();
        for(int i = 0; i < size; i++){
            DownloadCallbackWrapper wrapper = mTaskList.valueAt(i);
            if(null != wrapper){
                wrapper.removeCallback(callback);
//                break;
            }
        }
    }

    private static final int CALLBACK_CMD_START = 1;
    private static final int CALLBACK_CMD_CONNECT = 2;
    private static final int CALLBACK_CMD_PROGRESS = 3;
    private static final int CALLBACK_CMD_COMPLETE = 4;
    private static final int CALLBACK_CMD_ERROR = 5;
    private static final int CALLBACK_CMD_CANCEL = 6;
    private static final int CALLBACK_CMD_WARN = 7;
    private static final int CALLBACK_CMD_RETRY = 8;
    private static final int CALLBACK_CMD_FINALLY = 9;

    private final List<DownloadCallback> mDownloadCallbackList = new ArrayList<>();
    private DownloadCallbackWrapper(DownloadCallback downloadCallback){
        addCallback(downloadCallback);
    }

    public void addCallback(DownloadCallback downloadCallback){
        mDownloadCallbackList.add(downloadCallback);
    }

    public void removeCallback(DownloadCallback downloadCallback){
        mDownloadCallbackList.remove(downloadCallback);
    }

    public void clearCallback(){
        mDownloadCallbackList.clear();
    }

    @Override
    protected final void started(@NonNull DownloadTask task) {
        dispatchCallbackCmd(CALLBACK_CMD_START, task);
    }

    @Override
    protected final void completed(@NonNull DownloadTask task) {
        dispatchCallbackCmd(CALLBACK_CMD_COMPLETE, task);
    }

    @Override
    protected final void canceled(@NonNull DownloadTask task) {
        dispatchCallbackCmd(CALLBACK_CMD_CANCEL, task);
    }

    @Override
    protected final void error(@NonNull DownloadTask task, @NonNull Exception e) {
        ParameterBean parameterBean = new ParameterBean(task);
        parameterBean.object_1 = e;
        dispatchCallbackCmd(CALLBACK_CMD_ERROR, parameterBean);
    }

    @Override
    protected final void warn(@NonNull DownloadTask task) {
        dispatchCallbackCmd(CALLBACK_CMD_WARN, task);
    }

    @Override
    public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause,
                        @Nullable Exception realCause, @NonNull Listener1Assist.Listener1Model model) {
        printe("===TaskEnd -> : " + cause);
        if(DownloadUtils.isEnableLog() && null != realCause){
            printe("===Exception -> : " + realCause.getMessage());
            realCause.printStackTrace();
        }

        //@CallSuper
        super.taskEnd(task, cause, realCause, model);
        onFinally(task);
    }

    @Override
    public final void retry(@NonNull DownloadTask task, @NonNull ResumeFailedCause cause) {
        ParameterBean parameterBean = new ParameterBean(task);
        parameterBean.object_1 = cause;
        dispatchCallbackCmd(CALLBACK_CMD_RETRY, parameterBean);
    }

    @Override
    public final void connected(@NonNull DownloadTask task, int blockCount, long currentOffset, long totalLength) {
        ParameterBean parameterBean = new ParameterBean(task);
        parameterBean.int_1 = blockCount;
        parameterBean.long_1 = currentOffset;
        parameterBean.long_2 = totalLength;
        dispatchCallbackCmd(CALLBACK_CMD_CONNECT, parameterBean);
    }

    @Override
    public final void progress(@NonNull DownloadTask task, long currentOffset, long totalLength) {
        ParameterBean parameterBean = new ParameterBean(task);
        parameterBean.long_1 = currentOffset;
        parameterBean.long_2 = totalLength;
        dispatchCallbackCmd(CALLBACK_CMD_PROGRESS, parameterBean);
    }

    private void onFinally(DownloadTask task){
        dispatchCallbackCmd(CALLBACK_CMD_FINALLY, task);
        mDownloadCallbackList.clear();
    }

    private void dispatchCallbackCmd(int cmd, DownloadTask task){
        dispatchCallbackCmd(cmd, new ParameterBean(task));
    }

    private void dispatchCallbackCmd(int cmd, ParameterBean parameterBean){
        for(DownloadCallback downloadCallback : mDownloadCallbackList) {
            parameterBean.callback = downloadCallback;
            handle(cmd, parameterBean);
        }
    }

    private void handle(int cmd, ParameterBean parameterBean){
        DownloadTask task = parameterBean.task;
        DownloadCallback downloadCallback = parameterBean.callback;
        if(null == task || null == downloadCallback){
            printe("---ERROR -> task: " + task + "  callback: " + downloadCallback);
            return;
        }

        String cmdString = "Unknown";
        switch(cmd){
            default: printe("---ERROR, un handle cmd: " + cmdString + "(" + cmd + ") ERROR---");
                return;

            case CALLBACK_CMD_START:
                cmdString = "Start";
                downloadCallback.onStart(task);
                break;
            case CALLBACK_CMD_CONNECT:
                cmdString = "Connect";
                downloadCallback.onConnected(task, parameterBean.int_1,
                        parameterBean.long_1, parameterBean.long_2);
                break;
            case CALLBACK_CMD_PROGRESS:
                cmdString = "Progress";
                downloadCallback.onProgress(task,
                        downloadCallback.getPercent(parameterBean.long_1, parameterBean.long_2));
                break;
            case CALLBACK_CMD_COMPLETE:
                cmdString = "Complete";
                downloadCallback.onCompleted(task);
                break;
            case CALLBACK_CMD_CANCEL:
                cmdString = "Cancel";
                downloadCallback.onCancel(task);
                break;
            case CALLBACK_CMD_ERROR:
                cmdString = "Error";
                downloadCallback.onError(task, (Exception) parameterBean.object_1);
                break;
            case CALLBACK_CMD_RETRY:
                cmdString = "Retry";
                downloadCallback.onRetry(task, (ResumeFailedCause) parameterBean.object_1);
                break;
            case CALLBACK_CMD_WARN:
                cmdString = "Warn";
                downloadCallback.onWarn(task);
                break;
            case CALLBACK_CMD_FINALLY:
                cmdString = "Finally";
                downloadCallback.onFinally(task);
                break;
        }
        print("---handle callback cmd: <" + cmdString + ">, task id: " + task.getId());
    }

    private static void print(String s){
        DownloadUtils.printi(s);
    }

    private static void printe(String s){
        DownloadUtils.printe(s);
    }

    private class ParameterBean{
        DownloadTask task;
        DownloadCallback callback;

        int int_1;
        long long_1;
        long long_2;
        Object object_1;

        ParameterBean(DownloadTask task){
            this.task = task;
        }
    }
}
