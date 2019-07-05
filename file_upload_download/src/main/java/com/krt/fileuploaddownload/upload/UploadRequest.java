package com.krt.fileuploaddownload.upload;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class UploadRequest {
    public String serverUrl;
    private final Map<String, Object> formData = new HashMap<>();
    public AbsUploadCallback callback;
    public boolean needProgress = true;
    public boolean syncRequest = false;
    boolean taskIsExist = false;

    public UploadRequest addFormData(String formName, String formValue){
        formData.put(formName, formValue);
        return this;
    }

    public UploadRequest addFileFormData(String formName, String formValue){
        return addFileFormData(formName, new File(formValue));
    }

    public UploadRequest addFileFormData(String formName, File formValue){
        formData.put(formName, formValue);
        return this;
    }

    public Map<String, Object> getFormData(){
        return formData;
    }
}
