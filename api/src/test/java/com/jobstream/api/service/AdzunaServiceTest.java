package com.jobstream.api.service;

import com.jobstream.api.client.AdzunaClient;
import com.jobstream.api.exception.ResourceNotFoundException;
import com.jobstream.api.mapper.AdzunaMapper;
import com.jobstream.dto.JobDto;
import com.jobstream.dto.AdzunaJobSearchResponse;
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
class AdzunaServiceTest {

    @Mock
    private AdzunaClient adzunaClient;

    @Mock
    private AdzunaMapper adzunaMapper;

    @InjectMocks
    private AdzunaService adzunaService;

    @Test
    void getJobById_shouldReturnJobWhenIdMatches() {
        Map<String, Object> rawResponse = Map.of("count", 1);
        JobDto job = new JobDto("job_1", "Java Developer", "TechCorp", "Paris");
        AdzunaJobSearchResponse mapped = new AdzunaJobSearchResponse();
        mapped.setJobs(List.of(job));

        when(adzunaClient.callAdzunaApi("job_1", null, null, null)).thenReturn(rawResponse);
        when(adzunaMapper.toJobSearchResponse(rawResponse)).thenReturn(mapped);

        JobDto result = adzunaService.getJobById("job_1");

        assertThat(result).isSameAs(job);
        verify(adzunaClient).callAdzunaApi("job_1", null, null, null);
        verify(adzunaMapper).toJobSearchResponse(rawResponse);
    }

    @Test
    void getJobById_shouldThrowNotFoundWhenNoIdMatches() {
        Map<String, Object> rawResponse = Map.of("results", List.of());
        AdzunaJobSearchResponse mapped = new AdzunaJobSearchResponse();
        mapped.setJobs(List.of(new JobDto("job_999", "Another job", "Company", "Lyon")));

        when(adzunaClient.callAdzunaApi("job_1", null, null, null)).thenReturn(rawResponse);
        when(adzunaMapper.toJobSearchResponse(rawResponse)).thenReturn(mapped);

        assertThatThrownBy(() -> adzunaService.getJobById("job_1"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("job_1");
    }
}
