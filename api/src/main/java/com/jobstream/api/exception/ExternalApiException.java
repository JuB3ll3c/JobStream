package com.jobstream.api.exception;

/**
 * Exception thrown on errors with external APIs (Adzuna, etc.)
 */
public class ExternalApiException extends RuntimeException {

    private final String apiName;
    private final int statusCode;

    public ExternalApiException(String message) {
        super(message);
        this.apiName = "unknown";
        this.statusCode = 0;
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
        this.apiName = "unknown";
        this.statusCode = 0;
    }

    public ExternalApiException(String apiName, String message, int statusCode) {
        super(message);
        this.apiName = apiName;
        this.statusCode = statusCode;
    }

    public ExternalApiException(String apiName, String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.apiName = apiName;
        this.statusCode = statusCode;
    }

    public String getApiName() {
        return apiName;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
