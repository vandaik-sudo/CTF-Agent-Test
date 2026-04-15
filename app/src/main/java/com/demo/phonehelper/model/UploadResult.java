package com.demo.phonehelper.model;

public class UploadResult {
    private final boolean success;
    private final int responseCode;
    private final String responsePreview;
    private final String destination;
    private final int sentBytes;

    public UploadResult(boolean success, int responseCode, String responsePreview, String destination, int sentBytes) {
        this.success = success;
        this.responseCode = responseCode;
        this.responsePreview = responsePreview;
        this.destination = destination;
        this.sentBytes = sentBytes;
    }

    public static UploadResult failure(String destination, String responsePreview, int sentBytes) {
        return new UploadResult(false, -1, responsePreview, destination, sentBytes);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponsePreview() {
        return responsePreview;
    }

    public String getDestination() {
        return destination;
    }

    public int getSentBytes() {
        return sentBytes;
    }
}
