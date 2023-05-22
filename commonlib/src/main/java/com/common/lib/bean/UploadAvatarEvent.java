package com.common.lib.bean;

public class UploadAvatarEvent {

    private boolean isSuccess;
    private String url;
    private String error;

    public UploadAvatarEvent(boolean isSuccess, String url, String error) {
        this.isSuccess = isSuccess;
        this.url = url;
        this.error = error;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
