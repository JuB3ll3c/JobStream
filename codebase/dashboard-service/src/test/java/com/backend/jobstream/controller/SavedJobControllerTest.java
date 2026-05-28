package com.backend.jobstream.controller;

import com.jobstream.dto.PagedSavedJobResponse;
import com.jobstream.dto.SaveJobRequest;
import com.jobstream.dto.SavedJobDto;
import com.backend.jobstream.entity.SavedJob;
import com.backend.jobstream.mapper.SavedJobMapper;
import com.backend.jobstream.repository.SavedJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SavedJobController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SavedJobController Tests")
class SavedJobControllerTest {

    @Mock
    private SavedJobRepository savedJobRepository;

    @Mock
    private SavedJobMapper savedJobMapper;

    @InjectMocks
    private SavedJobController savedJobController;

    private SaveJobRequest validRequest;
    private SavedJob savedJobEntity;
    private SavedJobDto savedJobDto;

    @BeforeEach
    void setUp() {
        // Create a valid request
        validRequest = new SaveJobRequest();
        validRequest.setExternalId("job-123");
        validRequest.setTitle("Développeur Java");
        validRequest.setCompany("TechCorp");
        validRequest.setLocation("Paris");
        validRequest.setDescription("Description du poste");
        validRequest.setContractType("CDI");
        validRequest.setPostedDate(LocalDate.of(2026, 3, 25));
        validRequest.setJobUrl("https://example.com/job/123");

        // Create saved job entity
        savedJobEntity = new SavedJob();
        savedJobEntity.setId(UUID.randomUUID());
        savedJobEntity.setExternalId("job-123");
        savedJobEntity.setTitle("Développeur Java");
        savedJobEntity.setCompany("TechCorp");
        savedJobEntity.setLocation("Paris");
        savedJobEntity.setSavedAt(LocalDateTime.now());

        // Create saved job DTO
        savedJobDto = new SavedJobDto();
        savedJobDto.setId(UUID.randomUUID());
        savedJobDto.setExternalId("job-123");
        savedJobDto.setTitle("Développeur Java");
    }

    @Test
    @DisplayName("saveJob should save a new job successfully")
    void saveJob_shouldSaveNewJob() {
        // Given
        when(savedJobRepository.existsByExternalId("job-123")).thenReturn(false);
        when(savedJobMapper.toEntity(validRequest)).thenReturn(savedJobEntity);
        when(savedJobRepository.save(any(SavedJob.class))).thenReturn(savedJobEntity);
        when(savedJobMapper.toDto(savedJobEntity)).thenReturn(savedJobDto);

        // When
        ResponseEntity<SavedJobDto> response = savedJobController.saveJob(validRequest);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("job-123", response.getBody().getExternalId());
        
        verify(savedJobMapper).toEntity(validRequest);
        verify(savedJobRepository).save(any(SavedJob.class));
        verify(savedJobMapper).toDto(savedJobEntity);
    }

    @Test
    @DisplayName("saveJob should throw exception when job already exists")
    void saveJob_shouldThrowWhenJobExists() {
        // Given
        when(savedJobRepository.existsByExternalId("job-123")).thenReturn(true);

        // When/Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> savedJobController.saveJob(validRequest));
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("déjà sauvegardée"));
        
        verify(savedJobRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveJob should save job with all optional fields")
    void saveJob_shouldSaveWithAllFields() {
        // Given
        when(savedJobRepository.existsByExternalId(anyString())).thenReturn(false);
        
        ArgumentCaptor<SavedJob> captor = ArgumentCaptor.forClass(SavedJob.class);
        when(savedJobMapper.toEntity(any(SaveJobRequest.class))).thenAnswer(invocation -> {
            SaveJobRequest req = invocation.getArgument(0);
            SavedJob job = new SavedJob();
            job.setExternalId(req.getExternalId());
            job.setTitle(req.getTitle());
            job.setCompany(req.getCompany());
            job.setLocation(req.getLocation());
            job.setDescription(req.getDescription());
            job.setContractType(req.getContractType());
            job.setPostedDate(req.getPostedDate());
            job.setJobUrl(req.getJobUrl());
            job.setRequirements(req.getRequirements());
            job.setBenefits(req.getBenefits());
            job.setSavedAt(LocalDateTime.now());
            job.setSourceApi("adzuna");
            return job;
        });
        
        when(savedJobRepository.save(captor.capture())).thenReturn(savedJobEntity);

        // When
        savedJobController.saveJob(validRequest);

        // Then
        SavedJob captured = captor.getValue();
        assertEquals("job-123", captured.getExternalId());
        assertEquals("Développeur Java", captured.getTitle());
        assertEquals("TechCorp", captured.getCompany());
        assertEquals("Paris", captured.getLocation());
        assertEquals("Description du poste", captured.getDescription());
        assertEquals("CDI", captured.getContractType());
        assertEquals(LocalDate.of(2026, 3, 25), captured.getPostedDate());
        assertEquals("https://example.com/job/123", captured.getJobUrl());
        assertEquals("adzuna", captured.getSourceApi());
    }

    @Test
    @DisplayName("getSavedJobs should return list of saved jobs")
    void getSavedJobs_shouldReturnJobsList() {
        // Given
        SavedJob job1 = new SavedJob();
        job1.setId(UUID.randomUUID());
        job1.setExternalId("job-1");
        job1.setTitle("Développeur Java");
        job1.setCompany("TechCorp");
        job1.setSavedAt(LocalDateTime.now());

        SavedJob job2 = new SavedJob();
        job2.setId(UUID.randomUUID());
        job2.setExternalId("job-2");
        job2.setTitle("Full Stack Developer");
        job2.setCompany("WebAgency");
        job2.setSavedAt(LocalDateTime.now());

        Page<SavedJob> page = new PageImpl<>(List.of(job1, job2));
        when(savedJobRepository.findAllByOrderBySavedAtDesc(any(Pageable.class))).thenReturn(page);
        
        when(savedJobMapper.toDto(any(SavedJob.class))).thenReturn(savedJobDto);

        // When
        ResponseEntity<PagedSavedJobResponse> response = savedJobController.getSavedJobs(0, 20, "savedAt,desc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
    }

    @Test
    @DisplayName("getSavedJobs should return empty list when no saved jobs")
    void getSavedJobs_shouldReturnEmptyList() {
        // Given
        Page<SavedJob> emptyPage = new PageImpl<>(List.of());
        when(savedJobRepository.findAllByOrderBySavedAtDesc(any(Pageable.class))).thenReturn(emptyPage);

        // When
        ResponseEntity<PagedSavedJobResponse> response = savedJobController.getSavedJobs(0, 20, "savedAt,desc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getContent().isEmpty());
    }

    @Test
    @DisplayName("deleteSavedJob should delete job by UUID")
    void deleteSavedJob_shouldDeleteByUuid() {
        // Given
        UUID jobId = UUID.randomUUID();
        when(savedJobRepository.existsById(jobId)).thenReturn(true);
        doNothing().when(savedJobRepository).deleteById(jobId);

        // When
        ResponseEntity<Void> response = savedJobController.deleteSavedJob(jobId.toString());

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(savedJobRepository).deleteById(jobId);
    }

    @Test
    @DisplayName("deleteSavedJob should throw 404 when job not found")
    void deleteSavedJob_shouldThrowWhenNotFound() {
        // Given
        UUID jobId = UUID.randomUUID();
        when(savedJobRepository.existsById(jobId)).thenReturn(false);

        // When/Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> savedJobController.deleteSavedJob(jobId.toString()));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("deleteSavedJob should throw 400 for invalid UUID")
    void deleteSavedJob_shouldThrowForInvalidUuid() {
        // Given
        String invalidId = "not-a-uuid";

        // When/Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> savedJobController.deleteSavedJob(invalidId));
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
