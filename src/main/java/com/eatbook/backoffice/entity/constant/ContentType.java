package com.eatbook.backoffice.entity.constant;

public enum ContentType {
    JPEG("image/jpeg"),
    PNG("image/png"),
    TXT("text/plain; charset=UTF-8"),
    PDF("application/pdf"),
    MP3("audio/mpeg"),
    WAV("audio/wav"),
    OGG("audio/ogg");

    private final String mimeType;

    ContentType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }
}