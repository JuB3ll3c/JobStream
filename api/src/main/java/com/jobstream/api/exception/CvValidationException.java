package com.jobstream.api.exception;

public class CvValidationException extends RuntimeException {
    public enum Reason {
        INVALID_FILE,
        TOO_LARGE,
        UNSUPPORTED_MEDIA_TYPE
    }

    private final Reason reason;

    public CvValidationException(Reason reason, String message) {
        this(reason, message, null);
    }

    public CvValidationException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
