package com.jobstream.api.service;

import com.jobstream.api.client.AdzunaClient;
import com.jobstream.api.exception.ResourceNotFoundException;
import com.jobstream.api.exception.ExternalApiException;
import com.jobstream.api.mapper.AdzunaMapper;
import com.jobstream.dto.JobDto;
import com.jobstream.dto.JobOfferSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AdzunaService {
    private final AdzunaClient adzunaClient;
    private final AdzunaMapper adzunaMapper;

    public JobOfferSearchResponse searchJobOffers(String title, String location, Integer page, Integer size) {
        try {
            return adzunaMapper.toJobOfferSearchResponse(
                    adzunaClient.callAdzunaApi(title, page, size, location), page, size);
        } catch (ExternalApiException ex) {
            throw new ExternalApiException("Adzuna", "Job search is temporarily unavailable", 503, ex);
        }
    }

    public JobDto getJobById(String externalId) {
        return adzunaMapper.toJobSearchResponse(
                        adzunaClient.callAdzunaApi(externalId, null, null, null)
                ).getJobs().stream()
                .filter(job -> externalId.equals(job.getExternalId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found with id: " + externalId));
    }
}
