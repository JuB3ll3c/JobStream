package com.jobstream.api.exception;

import com.jobstream.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Gestionnaire global des exceptions pour l'API REST.
 * Centralise la gestion des erreurs et fournit des réponses standardisées.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Gère les erreurs des APIs externes (Adzuna, etc.)
     */
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(ExternalApiException ex) {
        log.error("Erreur API externe [{}]: {}", ex.getApiName(), ex.getMessage(), ex);

        return build(HttpStatus.BAD_GATEWAY, "Erreur lors de la communication avec le service externe", ex.getMessage());
    }

    /**
     * Gère les erreurs de validation des paramètres (@NotBlank, @Size, @Min, @Max)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Erreur de validation: {}", ex.getMessage());

        List<String> violations = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        return build(HttpStatus.BAD_REQUEST, "Paramètres de requête invalides", String.join(", ", violations));
    }

    /**
     * Gère les erreurs de type de paramètre (ex: page=abc au lieu de page=1)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Erreur de type de paramètre: {}", ex.getMessage());

        String message = String.format("Le paramètre '%s' a une valeur invalide: %s",
                ex.getName(), ex.getValue());

        return build(HttpStatus.BAD_REQUEST, "Type de paramètre invalide", message);
    }

    /**
     * Gère les erreurs de parsing JSON (corps de requête invalide)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("Erreur de parsing JSON: {}", ex.getMessage(), ex);

        return build(HttpStatus.BAD_REQUEST, "Format de requête invalide", "Le corps de la requête n'est pas un JSON valide");
    }

    /**
     * Gère toutes les autres exceptions non prévues
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Erreur inattendue: {}", ex.getMessage(), ex);

        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne du serveur", "Une erreur inattendue s'est produite");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message) {
        ErrorResponse err = new ErrorResponse();
        err.setStatus(status.value());
        err.setError(error);
        err.setMessage(message);
        err.setDate(LocalDateTime.now());
        return ResponseEntity.status(status).body(err);
    }
}
