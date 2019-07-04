package com.krt.fileuploaddownload.upload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Response;

class UploadCallbackWrapper extends AbsUploadCallback{
    private static Map<String, UploadCallbackWrapper> mTaskList = new HashMap<>();

    private static String getTaskId(UploadRequest request){
        StringBuilder stringBuilder = new StringBuilder(request.serverUrl);
        Iterator<Map.Entry<String, Object>> iterator = request.getFormData().entrySet().iterator();
        Map.Entry<String, Object> next;
        while(iterator.hasNext()){
            next = iterator.next();
            Object value = next.getValue();

            stringBuilder.append(next.getKey());
            if(value instanceof String){
                stringBuilder.append(value);
            }else if(value instanceof File){
                stringBuilder.append(((File) value).getName());
            }
        }
        return stringBuilder.toString();
    }

    static UploadCallbackWrapper getCallbackWrapper(UploadRequest request){
        UploadCallbackWrapper callbackWrapper;
        String taskId = getTaskId(request);
        request.callback.taskId = taskId;

        print("---Upload task id: " + taskId);
        UploadCallbackWrapper uploadCallbackWrapper = mTaskList.get(taskId);
        if(null != uploadCallbackWrapper){
            print("Upload task is exist: " + uploadCallbackWrapper);
            request.taskIsExist = true;
            uploadCallbackWrapper.addCallback(request.callback);
            return uploadCallbackWrapper;
        }
        print("Upload task is not exist.");
        callbackWrapper = new UploadCallbackWrapper(request.callback);
        mTaskList.put(taskId, callbackWrapper);
        return callbackWrapper;
    }

    static void unregisterCallback(AbsUploadCallback callback){
        Iterator<Map.Entry<String, UploadCallbackWrapper>> iterator = mTaskList.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, UploadCallbackWrapper> next = iterator.next();
            next.getValue().removeCallback(callback);
        }
    }

    private static void clearTask(List<String> taskIds){
        if(null == taskIds || taskIds.size() <= 0){
            return;
        }
        for(String taskId : taskIds){
            mTaskList.remove(taskId);
        }
    }

    private static final int CALLBACK_CMD_START = 1;
    private static final int CALLBACK_CMD_PROGRESS = 3;
    private static final int CALLBACK_CMD_COMPLETE = 4;
    private static final int CALLBACK_CMD_ERROR = 5;
    private static final int CALLBACK_CMD_CANCEL = 6;
    private static final int CALLBACK_CMD_FINALLY = 9;
    private static final int CALLBACK_CMD_PROGRESS_END = 10;

    private final Set<AbsUploadCallback> mUploadCallbackList = new HashSet<>();
    private UploadCallbackWrapper(AbsUploadCallback uploadCallback){
        addCallback(uploadCallback);
    }

    public void addCallback(AbsUploadCallback uploadCallback){
        mUploadCallbackList.add(uploadCallback);
    }

    public void removeCallback(AbsUploadCallback uploadCallback){
        mUploadCallbackList.remove(uploadCallback);
    }

    public void clearCallback(){
        mUploadCallbackList.clear();
    }

    @Override
    protected String getPercent(long currentPosition, long totalLength) {
        return super.getPercent(currentPosition, totalLength);
    }

    @Override
    public void onProgressStart(long totalBytes) {
        ParameterBean parameterBean = new ParameterBean();
        parameterBean.long_1 = totalBytes;
        dispatchCallbackCmd(CALLBACK_CMD_START, parameterBean);
    }

    @Override
    public void onProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
        ParameterBean parameterBean = new ParameterBean();
        parameterBean.long_1 = numBytes;
        parameterBean.long_2 = totalBytes;
        parameterBean.float_1 = percent;
        parameterBean.float_2 = speed;
        dispatchCallbackCmd(CALLBACK_CMD_PROGRESS, parameterBean);
    }

    @Override
    public void onProgressFinish() {
        ParameterBean parameterBean = new ParameterBean();
        dispatchCallbackCmd(CALLBACK_CMD_PROGRESS_END, parameterBean);
    }

    @Override
    public void onFailure(Call call, IOException e) {
        ParameterBean parameterBean = new ParameterBean();
        parameterBean.call = call;
        parameterBean.object_1 = e;
        dispatchCallbackCmd(CALLBACK_CMD_ERROR, parameterBean);
        onFinally(call, null, e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        ParameterBean parameterBean = new ParameterBean();
        parameterBean.call = call;
        parameterBean.object_1 = response;
        dispatchCallbackCmd(CALLBACK_CMD_COMPLETE, parameterBean);
        onFinally(call, response, null);
    }

    private void onFinally(Call call, Response response, Exception e) {
        printe("===UploadTaskEnd -> : " + call.toString());
        if(null != response){
            print(response.toString());
        }
        if(UploadUtils.isEnableLog() && null != e){
            printe("===Exception -> : " + e.getMessage());
            e.printStackTrace();
        }

        List<String> taskIds = new ArrayList<>();
        for(AbsUploadCallback callback : mUploadCallbackList){
            taskIds.add(callback.taskId);
        }
        clearTask(taskIds);
        clearCallback();
        dispatchCallbackCmd(CALLBACK_CMD_FINALLY);
    }

    private void dispatchCallbackCmd(int cmd){
        dispatchCallbackCmd(cmd, new ParameterBean());
    }

    private void dispatchCallbackCmd(int cmd, ParameterBean parameterBean){
        for(AbsUploadCallback uploadCallback : mUploadCallbackList) {
            parameterBean.callback = uploadCallback;
            handle(cmd, parameterBean);
        }
    }

    private void handle(int cmd, ParameterBean parameterBean){
        Call call = parameterBean.call;
        AbsUploadCallback uploadCallback = parameterBean.callback;
        if(null == uploadCallback){
            printe("---ERROR -> call: " + call + "  callback: " + uploadCallback);
            return;
        }

        String cmdString = "Unknown";
        switch(cmd){
            default: printe("---ERROR, un handle cmd: " + cmdString + "(" + cmd + ") ERROR---");
                return;

            case CALLBACK_CMD_START:
                cmdString = "Start";
                uploadCallback.onProgressStart(parameterBean.long_1);
                break;
            case CALLBACK_CMD_PROGRESS:
                cmdString = "Progress";
                uploadCallback.onProgressChanged(parameterBean.long_1, parameterBean.long_2,
                        parameterBean.float_1, parameterBean.float_2);
                break;
            case CALLBACK_CMD_PROGRESS_END:
                cmdString = "ProgressFinish";
                uploadCallback.onProgressFinish();
                break;
            case CALLBACK_CMD_COMPLETE:
                cmdString = "Complete";
                try {
                    uploadCallback.onResponse(parameterBean.call, (Response) parameterBean.object_1);
                } catch (Exception e) {
                    printe("---ERROR -> response handle error");
                    e.printStackTrace();
                    uploadCallback.onFailure(parameterBean.call, new IOException(e.getMessage()));
                }
                break;
            case CALLBACK_CMD_CANCEL:
                cmdString = "Cancel";
                uploadCallback.onCancel(parameterBean.call);
                break;
            case CALLBACK_CMD_ERROR:
                cmdString = "Error";
                uploadCallback.onFailure(parameterBean.call, (IOException) parameterBean.object_1);
                break;
            case CALLBACK_CMD_FINALLY:
                cmdString = "Finally";
                uploadCallback.onFinally();
                break;
        }
        print("---handle callback cmd: <" + cmdString + ">");
    }

    private static void print(String s){
        UploadUtils.printi(s);
    }

    private static void printe(String s){
        UploadUtils.printe(s);
    }

    private class ParameterBean{
        Call call;
        AbsUploadCallback callback;

        long long_1;
        long long_2;
        float float_1;
        float float_2;

        Object object_1;

    }
}
