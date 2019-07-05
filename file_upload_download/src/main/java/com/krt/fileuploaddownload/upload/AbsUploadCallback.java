package com.krt.fileuploaddownload.upload;

import com.krt.fileuploaddownload.upload.progress.ProgressListener;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public abstract class AbsUploadCallback extends ProgressListener implements Callback {

    protected void onConfig(Request.Builder requestBuilder){}

    @Override
    public final void onResponse(Call call, Response response) throws IOException {
        UploadResponse uploadResponse = new UploadResponse();
        try {
            uploadResponse.setMessage(response.message());
            uploadResponse.setBody(response.body().string());
            uploadResponse.setHeaders(response.headers().toString());
            uploadResponse.setNetworkResponse(response.networkResponse().toString());
        }catch(Exception e){
            e.printStackTrace();
            onFailure(call, new IOException(e.getMessage()));
            return;
        }
        onResponse(call, uploadResponse);
    }

    protected abstract void onResponse(Call call, UploadResponse response);

    @Override
    public void onProgressChanged(long numBytes, long totalBytes, float percent, float speed) {}

    protected void onCancel(Call call){}

    protected void onFinally(){}

}
