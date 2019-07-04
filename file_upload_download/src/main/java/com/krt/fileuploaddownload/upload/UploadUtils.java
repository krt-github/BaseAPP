package com.krt.fileuploaddownload.upload;

import android.Manifest;
import android.util.Log;

import com.krt.fileuploaddownload.upload.progress.ProgressHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UploadUtils {

    public static String[] getPermissions(){
        return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET};
    }

    private static boolean mEnableLog = false;
    private static UploadUtils mInstance;
    private OkHttpClient mOkHttpClient;
    private UploadUtils(OkHttpClient okHttpClient){
        if(null == okHttpClient){
            mOkHttpClient = new OkHttpClient();
        }else{
            mOkHttpClient = okHttpClient;
        }
    }

    public static void init(boolean enableLog){
        init(enableLog, null);
    }

    public static void init(boolean enableLog, OkHttpClient okHttpClient){
        if(null == mInstance){
            synchronized (UploadUtils.class){
                if(null == mInstance){
                    mInstance = new UploadUtils(okHttpClient);
                }
            }
        }

        mEnableLog = enableLog;
    }

    public static List<UploadTask> uploads(UploadRequest... uploadRequest){
        if(null == uploadRequest || uploadRequest.length <= 0){
            return null;
        }

        List<UploadTask> res = new ArrayList<>();
        for(UploadRequest request : uploadRequest){
            res.add(upload(request));
        }
        return res;
    }

    public static UploadTask upload(UploadRequest uploadRequest){
        if(null == uploadRequest || null == mInstance){
            return null;
        }

        return mInstance.simpleUpload(uploadRequest);
    }

    public static void cancel(UploadTask uploadTask){
        if(null != uploadTask){
            uploadTask.cancel();
        }
    }

    public static void pause(UploadTask uploadTask){
        printe("-----Un implement-----");
//        if(null != uploadTask){
//            uploadTask.();
//        }
    }

    public static void resume(UploadTask uploadTask){
        printe("-----Un implement-----");
//        if(null != uploadTask){
//            uploadTask.();
//        }
    }

    private UploadTask simpleUpload(UploadRequest uploadRequest){
        return uploadFileWithProgress(uploadRequest);
    }

    private MultipartBody generateRequestBody(Map<String, Object> formDataMap){
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        Iterator<Map.Entry<String, Object>> iterator = formDataMap.entrySet().iterator();
        Map.Entry<String, Object> next;
        while(iterator.hasNext()){
            next = iterator.next();
            String key = next.getKey();
            Object value = next.getValue();

            if(value instanceof String){
                builder.addFormDataPart(key, (String) value);
            }else if(value instanceof File){
                File file = (File) value;
                builder.addFormDataPart(key, file.getName(),
                        RequestBody.create(MediaType.parse("multipart/form-data"), file));
            }
        }

        return builder.build();
    }

    private UploadTask uploadFileWithProgress(UploadRequest uploadRequest){
        UploadCallbackWrapper callbackWrapper = UploadCallbackWrapper.getCallbackWrapper(uploadRequest);
        if(uploadRequest.taskIsExist){
            //TODO
            printe("---Skip exist task---");
            return null;
        }

        MultipartBody multipartBody = generateRequestBody(uploadRequest.getFormData());

        RequestBody requestBody;
        if(uploadRequest.needProgress){
            requestBody = ProgressHelper.withProgress(multipartBody, callbackWrapper);
        }else{
            requestBody = multipartBody;
        }

        try{
            Request.Builder requestBuilder = new Request.Builder().url(uploadRequest.serverUrl);
            uploadRequest.callback.onConfig(requestBuilder);
            requestBuilder.post(requestBody);

            Call call = mOkHttpClient.newCall(requestBuilder.build());
            if(uploadRequest.syncRequest){
//                call.execute();
                printe("---You must call UploadTask.syncRequest() method when work on sync request mode.---");
            }else {
                call.enqueue(callbackWrapper);
            }
            return new UploadTask(call, uploadRequest.syncRequest);
        }catch(Exception e){
            e.printStackTrace();
            uploadRequest.callback.onFailure(null, new IOException(e.getMessage()));
        }

        return null;
    }

    static boolean isEnableLog(){
        return mEnableLog;
    }

    static void printi(String s){
        if(isEnableLog()) {
            Log.i("UploadUtils", s);
        }
    }

    static void printe(String s){
        if(isEnableLog()) {
            Log.e("UploadUtils", s);
        }
    }

}
