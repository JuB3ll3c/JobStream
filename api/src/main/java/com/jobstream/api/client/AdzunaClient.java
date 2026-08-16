package com.jobstream.api.client;

import com.jobstream.api.exception.ExternalApiException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
@Log4j2
public class AdzunaClient {
    public static final int PAGE_NUM = 1;
    public static final int LIMIT_NUM = 20;
    private final RestClient restClient;
    private final String appId;
    private final String appKey;
    private final String defaultCountry;

    public AdzunaClient(RestClient.Builder restClientBuilder,
                        @Value("${adzuna.api.app-id}") String appId,
                        @Value("${adzuna.api.app-key}") String appKey,
                        @Value("${adzuna.api.base-url}") String baseUrl,
                        @Value("${adzuna.api.default-country}") String defaultCountry) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.appId = appId;
        this.appKey = appKey;
        this.defaultCountry = defaultCountry;
    }

    public Map<String, Object> callAdzunaApi(String query, Integer page, Integer limit, String location) {
        long startTime = System.currentTimeMillis();
        int pageNum = page != null ? page : PAGE_NUM;
        int limitNum = limit != null ? limit : LIMIT_NUM;

        try {
            log.debug("Calling Adzuna API: query={}, page={}, limit={}", query, pageNum, limitNum);
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/jobs/{country}/search/{page}")
                                .queryParam("app_id", appId)
                                .queryParam("app_key", appKey)
                                .queryParam("results_per_page", limitNum)
                                .queryParam("what", query)
                                .queryParam("content-type", "application/json");

                        if (location != null && !location.isBlank()) {
                            builder.queryParam("where", location);
                        }

                        return builder.build(Map.of(
                                "country", defaultCountry,
                                "page", String.valueOf(pageNum)
                        ));
                    })
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            long duration = System.currentTimeMillis() - startTime;
            log.debug("Adzuna API call completed in {}ms", duration);

            return response;

        } catch (RestClientException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error while calling Adzuna API after {}ms: {}", duration, e.getMessage());
            throw new ExternalApiException("Adzuna", "Error while searching for jobs", HttpStatus.BAD_GATEWAY.value(), e);
        }
    }
}
