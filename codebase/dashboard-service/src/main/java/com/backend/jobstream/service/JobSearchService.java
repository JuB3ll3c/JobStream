package com.backend.jobstream.service;

import com.jobstream.dto.JobDto;
import com.jobstream.dto.JobSearchResponse;

/**
 * Interface for job search operations.
 * Allows for multiple implementations (Adzuna, JSearch, etc.)
 */
public interface JobSearchService {

    /**
     * Search for job listings by keyword
     *
     * @param query    the search keyword
     * @param page     page number (default: 1)
     * @param limit    number of results per page (default: 20)
     * @param location location for the search (optional)
     * @return JobSearchResponse containing the list of jobs
     */
    JobSearchResponse searchJobs(String query, Integer page, Integer limit, String location);

    /**
     * Get job details by external ID
     *
     * @param externalId the external ID of the job
     * @return JobDto with job details
     */
    JobDto getJobByExternalId(String externalId);
}
