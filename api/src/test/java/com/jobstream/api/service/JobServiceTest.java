package com.jobstream.api.service;

import com.jobstream.api.client.AdzunaClient;
import com.jobstream.api.exception.ResourceNotFoundException;
import com.jobstream.api.mapper.AdzunaMapper;
import com.jobstream.dto.JobDto;
import com.jobstream.dto.JobSearchResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private AdzunaClient adzunaClient;

    @Mock
    private AdzunaMapper adzunaMapper;

    @InjectMocks
    private JobService jobService;

    @Test
    void searchJobs_shouldDelegateToClientAndMapResult() {
        Map<String, Object> rawResponse = Map.of("count", 1);
        JobSearchResponse mapped = new JobSearchResponse();
        mapped.setJobs(List.of(new JobDto("job_1", "Titre", "Société", "Lyon")));
        mapped.setTotal(1);
        mapped.setCount(1);

        when(adzunaClient.callAdzunaApi("java", 1, 20, "paris")).thenReturn(rawResponse);
        when(adzunaMapper.toJobSearchResponse(rawResponse)).thenReturn(mapped);

        JobSearchResponse result = jobService.searchJobs("java", 1, 20, "paris");

        assertThat(result).isSameAs(mapped);
        verify(adzunaClient).callAdzunaApi("java", 1, 20, "paris");
        verify(adzunaMapper).toJobSearchResponse(rawResponse);
    }

    @Test
    void searchJobs_shouldPassNullOptionalParameters() {
        Map<String, Object> rawResponse = Map.of();
        JobSearchResponse mapped = new JobSearchResponse();

        when(adzunaClient.callAdzunaApi("java", null, null, null)).thenReturn(rawResponse);
        when(adzunaMapper.toJobSearchResponse(rawResponse)).thenReturn(mapped);

        jobService.searchJobs("java", null, null, null);

        verify(adzunaClient).callAdzunaApi("java", null, null, null);
    }

    @Test
    void getJobById_shouldReturnJobWhenIdMatches() {
        Map<String, Object> rawResponse = Map.of("count", 1);
        JobDto job = new JobDto("job_1", "Développeur Java", "TechCorp", "Paris");
        JobSearchResponse mapped = new JobSearchResponse();
        mapped.setJobs(List.of(job));

        when(adzunaClient.callAdzunaApi("job_1", null, null, null)).thenReturn(rawResponse);
        when(adzunaMapper.toJobSearchResponse(rawResponse)).thenReturn(mapped);

        JobDto result = jobService.getJobById("job_1");

        assertThat(result).isSameAs(job);
        verify(adzunaClient).callAdzunaApi("job_1", null, null, null);
        verify(adzunaMapper).toJobSearchResponse(rawResponse);
    }

    @Test
    void getJobById_shouldThrowNotFoundWhenNoIdMatches() {
        Map<String, Object> rawResponse = Map.of("results", List.of());
        JobSearchResponse mapped = new JobSearchResponse();
        mapped.setJobs(List.of(new JobDto("job_999", "Autre offre", "Boite", "Lyon")));

        when(adzunaClient.callAdzunaApi("job_1", null, null, null)).thenReturn(rawResponse);
        when(adzunaMapper.toJobSearchResponse(rawResponse)).thenReturn(mapped);

        assertThatThrownBy(() -> jobService.getJobById("job_1"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("job_1");
    }
}