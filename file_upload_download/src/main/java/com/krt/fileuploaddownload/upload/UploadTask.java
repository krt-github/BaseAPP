package com.krt.fileuploaddownload.upload;

import java.io.IOException;

import okhttp3.Call;

public class UploadTask {
    private boolean syncRequest = false;
    private Call uploadCall;

    UploadTask(Call call, boolean sync){
        uploadCall = call;
        syncRequest = sync;
    }

    public void syncRequest() throws IOException {
        if(syncRequest && null != uploadCall && !uploadCall.isCanceled()){
            uploadCall.execute();
        }
    }

    public void cancel(){
        if(null != uploadCall){
            UploadCallbackWrapper.cancelTask(uploadCall);
        }
    }
}
