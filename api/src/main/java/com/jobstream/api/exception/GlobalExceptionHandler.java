package com.jobstream.api.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.jobstream.dto.ErrorResponse;
import java.time.LocalDate;
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


        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.BAD_GATEWAY.value());
        error.setError("Erreur lors de la communication avec le service externe");
        error.setMessage(ex.getMessage());
        error.setDate(LocalDate.now());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
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

        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError("Paramètres de requête invalides");
        error.setMessage(String.join(", ", violations));
        error.setDate(LocalDate.now());

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Gère les erreurs de type de paramètre (ex: page=abc au lieu de page=1)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Erreur de type de paramètre: {}", ex.getMessage());

        String message = String.format("Le paramètre '%s' a une valeur invalide: %s",
                ex.getName(), ex.getValue());

        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError("Type de paramètre invalide");
        error.setMessage(message);
        error.setDate(LocalDate.now());

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Gère les erreurs de parsing JSON (corps de requête invalide)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("Erreur de parsing JSON: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError("Format de requête invalide");
        error.setMessage("Le corps de la requête n'est pas un JSON valide");
        error.setDate(LocalDate.now());

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Gère toutes les autres exceptions non prévues
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Erreur inattendue: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setError("Erreur interne du serveur");
        error.setMessage("Une erreur inattendue s'est produite");
        error.setDate(LocalDate.now());

        return ResponseEntity.internalServerError().body(error);
    }
}
