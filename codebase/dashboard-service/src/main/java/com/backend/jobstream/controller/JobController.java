package com.backend.jobstream.controller;

import com.jobstream.dto.JobDto;
import com.jobstream.dto.JobSearchResponse;
import com.backend.jobstream.service.JobSearchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller pour la recherche d'offres d'emploi
 * Endpoint: GET /api/jobs?q={motCle}
 */
@RestController
@RequestMapping("/api/jobs")
@Validated
public class JobController {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);
    private static final int MAX_QUERY_LENGTH = 200;
    private static final int MAX_LIMIT = 100;

    private final JobSearchService jobSearchService;

    public JobController(JobSearchService jobSearchService) {
        this.jobSearchService = jobSearchService;
        log.info("Service de recherche d'offres configuré");
    }

    /**
     * Recherche des offres d'emploi par mot-clé
     *
     * @param q        le mot-clé de recherche (obligatoire)
     * @param page     numéro de page (optionnel, défaut: 1)
     * @param limit    nombre de résultats par page (optionnel, défaut: 20)
     * @param location localisation pour la recherche (optionnel)
     * @return la liste des offres correspondant à la recherche
     */
    @GetMapping
    public ResponseEntity<JobSearchResponse> searchJobs(
            @RequestParam @NotBlank @Size(max = MAX_QUERY_LENGTH) String q,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_LIMIT) Integer limit,
            @RequestParam(required = false) @Pattern(regexp = "^[a-zA-Z\\s\\-.,]+$", message = "Location must contain only letters, spaces, and basic punctuation") String location) {

        log.info("Recherche d'offres: q={}, page={}, limit={}, location={}", 
                q, page, limit, location);

        JobSearchResponse response = jobSearchService.searchJobs(q, page, limit, location);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les détails d'une offre par son ID externe
     *
     * @param externalId l'ID externe du job
     * @return les détails du job
     */
    @GetMapping("/{externalId}")
    public ResponseEntity<JobDto> getJobByExternalId(@PathVariable String externalId) {
        log.info("Récupération des détails du job: externalId={}", externalId);

        JobDto job = jobSearchService.getJobByExternalId(externalId);

        return ResponseEntity.ok(job);
    }
}
