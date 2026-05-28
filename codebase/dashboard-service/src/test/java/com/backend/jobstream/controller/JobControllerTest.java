package com.backend.jobstream.controller;

import com.jobstream.dto.JobDto;
import com.jobstream.dto.JobSearchResponse;
import com.backend.jobstream.service.AdzunaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JobController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JobController Tests")
class JobControllerTest {

    @Mock
    private AdzunaService adzunaService;

    @InjectMocks
    private JobController jobController;

    @Test
    @DisplayName("searchJobs should return jobs for valid query")
    void searchJobs_shouldReturnJobs() {
        // Given
        JobDto job = new JobDto();
        job.setExternalId("job-1");
        job.setTitle("Developer");
        job.setCompany("TechCorp");
        job.setLocation("Paris");
        
        JobSearchResponse response = new JobSearchResponse();
        response.setJobs(List.of(job));
        response.setTotal(1);
        response.setCount(1);

        when(adzunaService.searchJobs(anyString(), anyInt(), anyInt(), any()))
                .thenReturn(response);

        // When
        ResponseEntity<JobSearchResponse> result = jobController.searchJobs(
                "developer", 1, 20, null
        );

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getTotal());
    }

    @Test
    @DisplayName("searchJobs should return empty list for no results")
    void searchJobs_shouldReturnEmptyList() {
        // Given
        JobSearchResponse response = new JobSearchResponse();
        response.setJobs(List.of());
        response.setTotal(0);
        response.setCount(0);

        when(adzunaService.searchJobs(anyString(), anyInt(), anyInt(), any()))
                .thenReturn(response);

        // When
        ResponseEntity<JobSearchResponse> result = jobController.searchJobs(
                "xyzinvalide", 1, 20, null
        );

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(0, result.getBody().getTotal());
    }

    @Test
    @DisplayName("searchJobs should pass location parameter")
    void searchJobs_shouldPassLocationParameter() {
        // Given
        JobSearchResponse response = new JobSearchResponse();
        response.setJobs(List.of());
        response.setTotal(0);
        response.setCount(0);

        when(adzunaService.searchJobs(eq("developer"), eq(1), eq(20), eq("Paris")))
                .thenReturn(response);

        // When
        ResponseEntity<JobSearchResponse> result = jobController.searchJobs(
                "developer", 1, 20, "Paris"
        );

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    @DisplayName("searchJobs should use default pagination values")
    void searchJobs_shouldUseDefaultPagination() {
        // Given
        JobSearchResponse response = new JobSearchResponse();
        response.setJobs(List.of());
        response.setTotal(0);
        response.setCount(0);

        when(adzunaService.searchJobs(anyString(), eq(1), eq(20), isNull()))
                .thenReturn(response);

        // When
        ResponseEntity<JobSearchResponse> result = jobController.searchJobs(
                "developer", 1, 20, null
        );

        // Then
        assertNotNull(result);
    }
}
