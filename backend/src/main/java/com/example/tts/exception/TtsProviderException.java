package com.example.tts.exception;

public class TtsProviderException extends RuntimeException {
    private final int statusCode;

    public TtsProviderException(String message) {
        super(message);
        this.statusCode = 503;
    }

    public TtsProviderException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public TtsProviderException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
