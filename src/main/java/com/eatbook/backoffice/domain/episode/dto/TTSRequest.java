package com.eatbook.backoffice.domain.episode.dto;
public class TTSRequest {
    private String text;
    private String s3Key;

    public TTSRequest(String text, String s3Key) {
        this.text = text;
        this.s3Key = s3Key;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }
}