package com.krt.fileuploaddownload.upload;

public class UploadResponse {
    private String message;
    private String body;
    private String networkResponse;
    private String headers;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getNetworkResponse() {
        return networkResponse;
    }

    public void setNetworkResponse(String networkResponse) {
        this.networkResponse = networkResponse;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "UploadResponse{" +
                "message='" + message + '\'' +
                ", body='" + body + '\'' +
                ", networkResponse='" + networkResponse + '\'' +
                ", headers='" + headers + '\'' +
                '}';
    }
}
