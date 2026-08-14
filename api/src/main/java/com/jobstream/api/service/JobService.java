package com.jobstream.api.service;

import com.jobstream.api.client.AdzunaClient;
import com.jobstream.api.mapper.AdzunaMapper;
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
}
