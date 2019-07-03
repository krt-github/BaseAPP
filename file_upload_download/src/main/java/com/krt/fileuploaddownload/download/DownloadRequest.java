package com.krt.fileuploaddownload.download;

public class DownloadRequest {
    public String downloadUrl;
    public String saveDir;
    public String fileName;
    public DownloadCallback callback;
    public boolean syncRequest = false;

    public DownloadRequest(String downloadUrl, String saveDir) {
        this.downloadUrl = downloadUrl;
        this.saveDir = saveDir;
    }

    @Override
    public String toString() {
        return "DownloadRequest{" +
                "downloadUrl='" + downloadUrl + '\'' +
                ", saveDir='" + saveDir + '\'' +
                ", fileName='" + fileName + '\'' +
                ", callback=" + callback +
                ", syncRequest=" + syncRequest +
                '}';
    }
}
