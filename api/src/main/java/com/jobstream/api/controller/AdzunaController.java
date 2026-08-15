package com.jobstream.api.controller;

import com.jobstream.api.service.AdzunaService;
import com.jobstream.dto.AdzunaJobSearchResponse;
import com.jobstream.dto.JobDto;
import com.jobstream.endpoint.AdzunaApi;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller pour la recherche d'offres d'emploi
 * Endpoint: GET /api/jobs?q={motCle}
 */
@RestController
@RequiredArgsConstructor
public class AdzunaController implements AdzunaApi {

    private static final Logger log = LoggerFactory.getLogger(AdzunaController.class);

    private final AdzunaService adzunaService;

    /**
     * Recherche des offres d'emploi par mot-clé
     * (les contraintes de validation sont définies dans la spec OpenAPI)
     *
     * @param query        le mot-clé de recherche (obligatoire)
     * @param page     numéro de page (optionnel, défaut: 1)
     * @param limit    nombre de résultats par page (optionnel, défaut: 20)
     * @param location localisation pour la recherche (optionnel)
     * @return la liste des offres correspondant à la recherche
     */
    @Override
    public ResponseEntity<AdzunaJobSearchResponse> searchJobs(
            String query,
            Integer page,
            Integer limit,
            String location) {

        log.debug("Recherche d'offres: q={}, page={}, limit={}, location={}",
                query, page, limit, location);

        AdzunaJobSearchResponse response = adzunaService.searchJobs(query, page, limit, location);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<JobDto> getAdzunaJobById(String externalId) {
        return ResponseEntity.ok(adzunaService.getJobById(externalId));
    }
}