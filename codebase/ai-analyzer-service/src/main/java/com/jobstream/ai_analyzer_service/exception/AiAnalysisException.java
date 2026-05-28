package com.jobstream.ai_analyzer_service.exception;

/**
 * Exception levée lors d'une erreur lors de l'analyse IA
 */
public class AiAnalysisException extends RuntimeException {

    public AiAnalysisException(String message) {
        super(message);
    }

    public AiAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
