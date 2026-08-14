package com.jobstream.api.exception;

import com.jobstream.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleExternalApiException_shouldReturnBadGateway() {
        ExternalApiException ex = new ExternalApiException("Adzuna", "Erreur lors de la recherche d'offres", 502);

        ResponseEntity<ErrorResponse> response = handler.handleExternalApiException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(502);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(502);
        assertThat(response.getBody().getError()).isEqualTo("Erreur lors de la communication avec le service externe");
        assertThat(response.getBody().getMessage()).isEqualTo("Erreur lors de la recherche d'offres");
        assertThat(response.getBody().getDate()).isNotNull();
    }

    @Test
    void handleConstraintViolation_shouldReturnBadRequestWithViolations() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("query");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Paramètres de requête invalides");
        assertThat(response.getBody().getMessage()).contains("query: must not be blank");
    }

    @Test
    void handleTypeMismatch_shouldReturnBadRequestWithParamName() {
        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException("abc", Integer.class, "page", null, null);

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Type de paramètre invalide");
        assertThat(response.getBody().getMessage()).contains("page").contains("abc");
    }

    @Test
    void handleMessageNotReadable_shouldReturnBadRequest() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("malformed json", null);

        ResponseEntity<ErrorResponse> response = handler.handleMessageNotReadable(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Format de requête invalide");
        assertThat(response.getBody().getMessage()).isEqualTo("Le corps de la requête n'est pas un JSON valide");
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError() {
        Exception ex = new IllegalStateException("boom");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Erreur interne du serveur");
        assertThat(response.getBody().getMessage()).isEqualTo("Une erreur inattendue s'est produite");
    }
}