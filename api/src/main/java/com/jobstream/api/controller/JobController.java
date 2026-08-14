package com.jobstream.api.controller;

import com.jobstream.api.service.JobService;
import com.jobstream.dto.JobSearchResponse;
import com.jobstream.endpoint.JobsApi;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller pour la recherche d'offres d'emploi
 * Endpoint: GET /api/jobs?q={motCle}
 */
@RestController
@Validated
@AllArgsConstructor
public class JobController implements JobsApi {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);
    private static final int MAX_QUERY_LENGTH = 200;
    private static final int MAX_LIMIT = 100;

    private final JobService jobService;

    /**
     * Recherche des offres d'emploi par mot-clé
     *
     * @param query        le mot-clé de recherche (obligatoire)
     * @param page     numéro de page (optionnel, défaut: 1)
     * @param limit    nombre de résultats par page (optionnel, défaut: 20)
     * @param location localisation pour la recherche (optionnel)
     * @return la liste des offres correspondant à la recherche
     */
    @Override
    public ResponseEntity<JobSearchResponse> searchJobs(
            @RequestParam @NotBlank @Size(max = MAX_QUERY_LENGTH) String query,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_LIMIT) Integer limit,
            @RequestParam(required = false) @Pattern(regexp = "^[a-zA-Z\\s\\-.,]+$", message = "Location must contain only letters, spaces, and basic punctuation") String location) {

        log.info("Recherche d'offres: q={}, page={}, limit={}, location={}",
                query, page, limit, location);

        JobSearchResponse response = jobService.searchJobs(query, page, limit, location);

        return ResponseEntity.ok(response);
    }
}
