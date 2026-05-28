package com.jobstream.ai_analyzer_service.exception;

/**
 * Exception levée lors d'une erreur lors du chargement du fichier CV
 */
public class CvLoadException extends RuntimeException {

    public CvLoadException(String message) {
        super(message);
    }

    public CvLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
