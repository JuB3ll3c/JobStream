package com.jobstream.api.service;

import com.jobstream.api.client.AdzunaClient;
import com.jobstream.api.exception.ResourceNotFoundException;
import com.jobstream.api.mapper.AdzunaMapper;
import com.jobstream.dto.JobDto;
import com.jobstream.dto.JobSearchResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class JobService {
    private final AdzunaClient adzunaClient;
    private final AdzunaMapper adzunaMapper;

    public JobSearchResponse searchJobs(String query, Integer page, Integer limit, String location) {
        return adzunaMapper.toJobSearchResponse(
                adzunaClient.callAdzunaApi(query, page, limit, location)
        );
    }

    public JobDto getJobById(String externalId) {
        return adzunaMapper.toJobSearchResponse(
                        adzunaClient.callAdzunaApi(externalId, null, null, null)
                ).getJobs().stream()
                .filter(job -> externalId.equals(job.getExternalId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Offre introuvable avec l'id: " + externalId));
    }
}
