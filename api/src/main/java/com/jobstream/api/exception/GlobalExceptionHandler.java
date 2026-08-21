package com.jobstream.api.exception;

import com.jobstream.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Global exception handler for the REST API.
 * Centralizes error handling and provides standardized responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles errors from external APIs (Adzuna, etc.)
     */
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(ExternalApiException ex) {
        log.error("External API error [{}]: {}", ex.getApiName(), ex.getMessage(), ex);

        return build(HttpStatus.BAD_GATEWAY, "Error communicating with the external service", ex.getMessage());
    }

    /**
     * Handles parameter validation errors (@NotBlank, @Size, @Min, @Max)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        List<String> violations = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        return build(HttpStatus.BAD_REQUEST, "Invalid query parameters", String.join(", ", violations));
    }

    /**
     * Handles parameter type errors (e.g. page=abc instead of page=1)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Parameter type error: {}", ex.getMessage());

        String message = String.format("Parameter '%s' has an invalid value: %s",
                ex.getName(), ex.getValue());

        return build(HttpStatus.BAD_REQUEST, "Invalid parameter type", message);
    }

    /**
     * Handles JSON parsing errors (invalid request body)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("JSON parsing error: {}", ex.getMessage(), ex);

        return build(HttpStatus.BAD_REQUEST, "Invalid request format", "The request body is not valid JSON");
    }

    /**
     * Handles request body validation errors (@Valid on @RequestBody)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.warn("Request body validation error: {}", ex.getMessage());

        List<String> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        return build(HttpStatus.BAD_REQUEST, "Invalid request", String.join(", ", violations));
    }

    /**
     * Handles 404 NOT FOUND errors
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Job not found", ex.getMessage());
    }

    /**
     * Handles conflicts (e.g. job already saved)
     */
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ResourceConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "Resource conflict", ex.getMessage());
    }

    /**
     * Handles database constraint violations (e.g. unique constraint on external_id)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Database constraint violation: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "Resource conflict", "A similar resource already exists");
    }

    /**
     * Handles all other unexpected exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "An unexpected error occurred");
    }

    /**
     * Handles authentication errors
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return build(HttpStatus.UNAUTHORIZED, "Authentication failed", "Wrong email or password.");
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