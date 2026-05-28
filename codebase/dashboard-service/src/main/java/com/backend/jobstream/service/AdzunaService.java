package com.backend.jobstream.service;

import com.jobstream.dto.JobDto;
import com.jobstream.dto.JobSearchResponse;
import com.backend.jobstream.exception.ExternalApiException;
import com.backend.jobstream.mapper.AdzunaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service pour appeler l'API Adzuna
 * Documentation: https://developer.adzuna.com/docs/search
 */
@Service
public class AdzunaService implements JobSearchService {

    private static final Logger log = LoggerFactory.getLogger(AdzunaService.class);

    private final RestClient restClient;
    private final AdzunaMapper mapper;
    private final CountryDetector countryDetector;
    private final String appId;
    private final String appKey;

    public AdzunaService(
            RestClient.Builder restClientBuilder,
            AdzunaMapper mapper,
            CountryDetector countryDetector,
            @Value("${adzuna.api.app-id:}") String appId,
            @Value("${adzuna.api.app-key:}") String appKey,
            @Value("${adzuna.api.base-url:}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.mapper = mapper;
        this.countryDetector = countryDetector;
        this.appId = appId;
        this.appKey = appKey;
        
        if (appId == null || appId.isBlank() || appKey == null || appKey.isBlank()) {
            log.warn("Adzuna API credentials not configured. Set ADZUNA_APP_ID and ADZUNA_APP_KEY environment variables.");
        } else {
            log.info("Adzuna API configured");
        }
    }

    /**
     * Recherche des offres d'emploi via Adzuna API
     *
     * @param query    le mot-clé de recherche
     * @param page     numéro de page (défaut: 1)
     * @param limit    nombre de résultats par page (défaut: 20)
     * @param location localisation pour la recherche (optionnel)
     * @return JobSearchResponse contenant la liste des jobs
     * @throws ExternalApiException si l'API externe échoue
     */
    @Cacheable(value = "jobSearch", key = "#query + '-' + #page + '-' + #limit + '-' + #location")
    public JobSearchResponse searchJobs(String query, Integer page, Integer limit, String location) {
        log.info("Recherche jobs Adzuna (cache miss): query={}, page={}, limit={}, location={}", 
                query, page, limit, location);

        // Vérifier les credentials
        if (appId == null || appId.isBlank() || appKey == null || appKey.isBlank()) {
            throw new ExternalApiException("Adzuna", "API credentials not configured", 401, null);
        }

        return callAdzunaApi(query, page, limit, location);
    }

    /**
     * Récupère les détails d'une offre par son ID externe
     *
     * @param externalId l'ID externe du job
     * @return JobDto avec les détails du job
     * @throws ExternalApiException si l'API externe échoue
     */
    public JobDto getJobByExternalId(String externalId) {
        log.info("Récupération des détails du job: externalId={}", externalId);

        // Vérifier les credentials
        if (appId == null || appId.isBlank() || appKey == null || appKey.isBlank()) {
            throw new ExternalApiException("Adzuna", "API credentials not configured", 401, null);
        }

        return callAdzunaApiGetJob(externalId);
    }

    private JobDto callAdzunaApiGetJob(String externalId) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Adzuna doesn't have a direct "get by ID" endpoint, so we search by ID
            // Using the job_id parameter
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/v1/api/jobs/{country}/search/1")
                                .queryParam("app_id", appId)
                                .queryParam("app_key", appKey)
                                .queryParam("what", externalId)
                                .queryParam("results_per_page", 1);

                        String country = countryDetector.detectCountry(null);
                        return builder.build(Map.of("country", country));
                    })
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            long duration = System.currentTimeMillis() - startTime;
            log.info("Adzuna API call (get job) completed in {}ms", duration);

            return mapResponseGetJob(response, externalId);

        } catch (RestClientException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Erreur lors de l'appel à Adzuna API après {}ms: {}", duration, e.getMessage());
            throw new ExternalApiException("Adzuna", "Erreur lors de la récupération du job", 502, e);
        }
    }

    @SuppressWarnings("unchecked")
    private JobDto mapResponseGetJob(Map<String, Object> response, String externalId) {
        if (response == null) {
            throw new ExternalApiException("Adzuna", "Job not found", 404, null);
        }

        List<Map<String, Object>> jobDataList = (List<Map<String, Object>>) response.get("results");

        if (jobDataList == null || jobDataList.isEmpty()) {
            throw new ExternalApiException("Adzuna", "Job not found: " + externalId, 404, null);
        }

        // Find the job with matching ID
        for (Map<String, Object> jobData : jobDataList) {
            String jobId = (String) jobData.get("id");
            if (externalId.equals(jobId)) {
                return mapper.mapToJobDto(jobData);
            }
        }

        throw new ExternalApiException("Adzuna", "Job not found: " + externalId, 404, null);
    }

    private JobSearchResponse callAdzunaApi(String query, Integer page, Integer limit, String location) {
        long startTime = System.currentTimeMillis();
        
        try {
            int pageNum = page != null ? page : 1;
            int limitNum = limit != null ? limit : 20;
            
            log.debug("Calling Adzuna API: query={}, page={}, limit={}", query, pageNum, limitNum);
            
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/v1/api/jobs/{country}/search/{page}")
                                .queryParam("app_id", appId)
                                .queryParam("app_key", appKey)
                                .queryParam("results_per_page", limitNum)
                                .queryParam("what", query)
                                .queryParam("distance", 40)
                                .queryParam("content-type", "application/json");
                        
                        if (location != null && !location.isBlank()) {
                            builder.queryParam("where", location);
                        }

                        String country = countryDetector.detectCountry(location);

                        return builder.build(Map.of(
                                "country", country,
                                "page", String.valueOf(pageNum)
                        ));
                    })
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            long duration = System.currentTimeMillis() - startTime;
            log.info("Adzuna API call completed in {}ms", duration);

            logDebugResponseInfo(response);

            return mapResponse(response);

        } catch (RestClientException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Erreur lors de l'appel à Adzuna API après {}ms: {}", duration, e.getMessage());
            throw new ExternalApiException("Adzuna", "Erreur lors de la recherche d'offres", 502, e);
        }
    }

    /**
     * Logs debug information about the Adzuna API response
     * Only executes when debug logging is enabled
     */
    private void logDebugResponseInfo(Map<String, Object> response) {
        if (!log.isDebugEnabled() || response == null) {
            return;
        }

        log.debug("Adzuna response keys: {}", response.keySet());
        log.debug("Adzuna response 'count': {}", response.get("count"));
        log.debug("Adzuna response 'total': {}", response.get("total"));

        // Search for other possible fields
        for (String key : response.keySet()) {
            if (key.toLowerCase().contains("total") || key.toLowerCase().contains("count")) {
                log.debug("Adzuna response '{}': {}", key, response.get(key));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private JobSearchResponse mapResponse(Map<String, Object> response) {
        if (response == null) {
            return createEmptyResponse();
        }

        List<Map<String, Object>> jobDataList = (List<Map<String, Object>>) response.get("results");

        if (jobDataList == null || jobDataList.isEmpty()) {
            return createEmptyResponse();
        }

        List<JobDto> jobs = jobDataList.stream()
                .map(mapper::mapToJobDto)
                .toList();

        // Adzuna ne retourne pas toujours un total, on utilise count ou jobs.size()
        Integer count = extractNumber(response, "count");
        
        JobSearchResponse result = new JobSearchResponse();
        result.setJobs(jobs);
        result.setTotal(count != null ? count : jobs.size());
        result.setCount(jobs.size());
        
        log.info("Adzuna returned {} jobs, total count: {}", jobs.size(), count);
        
        return result;
    }

    private Integer extractNumber(Map<String, Object> response, String key) {
        Object value = response.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private JobSearchResponse createEmptyResponse() {
        JobSearchResponse result = new JobSearchResponse();
        result.setJobs(new ArrayList<>());
        result.setTotal(0);
        result.setCount(0);
        return result;
    }
}
