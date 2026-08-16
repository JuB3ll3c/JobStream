package com.jobstream.api.controller;

import com.jobstream.api.service.AdzunaService;
import com.jobstream.dto.AdzunaJobSearchResponse;
import com.jobstream.dto.JobDto;
import com.jobstream.endpoint.AdzunaApi;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for job search
 * Endpoint: GET /adzuna/jobs?q={keyword}
 */
@RestController
@RequiredArgsConstructor
public class AdzunaController implements AdzunaApi {

    private static final Logger log = LoggerFactory.getLogger(AdzunaController.class);

    private final AdzunaService adzunaService;

    /**
     * Search job offers by keyword
     * (validation constraints are defined in the OpenAPI spec)
     *
     * @param query    the search keyword (required)
     * @param page     page number (optional, default: 1)
     * @param limit    number of results per page (optional, default: 20)
     * @param location search location (optional)
     * @return the list of job offers matching the search
     */
    @Override
    public ResponseEntity<AdzunaJobSearchResponse> searchJobs(
            String query,
            Integer page,
            Integer limit,
            String location) {

        log.debug("Job search: q={}, page={}, limit={}, location={}",
                query, page, limit, location);

        AdzunaJobSearchResponse response = adzunaService.searchJobs(query, page, limit, location);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<JobDto> getAdzunaJobById(String externalId) {
        return ResponseEntity.ok(adzunaService.getJobById(externalId));
    }
}