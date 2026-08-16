package com.jobstream.api.service;

import com.jobstream.api.entity.Job;
import com.jobstream.api.exception.ResourceConflictException;
import com.jobstream.api.exception.ResourceNotFoundException;
import com.jobstream.api.mapper.JobMapper;
import com.jobstream.api.repository.JobRepository;
import com.jobstream.dto.JobDto;
import com.jobstream.dto.JobRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobService jobService;

    @Test
    void getJobById_shouldReturnDtoWhenFound() {
        Job job = new Job();
        JobDto dto = new JobDto("job_1", "Développeur Java", "TechCorp", "Paris");
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobMapper.toDto(job)).thenReturn(dto);

        JobDto result = jobService.getJobById(1L);

        assertThat(result).isSameAs(dto);
        verify(jobRepository).findById(1L);
        verify(jobMapper).toDto(job);
    }

    @Test
    void getJobById_shouldThrowNotFoundWhenMissing() {
        when(jobRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getJobById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void getJobs_shouldMapPage() {
        Job job = new Job();
        JobDto dto = new JobDto("job_1", "Développeur Java", "TechCorp", "Paris");
        Page<Job> page = new PageImpl<>(List.of(job));
        PageRequest pageable = PageRequest.of(0, 10);

        when(jobRepository.findAll(pageable)).thenReturn(page);
        when(jobMapper.toDto(job)).thenReturn(dto);

        Page<JobDto> result = jobService.getJobs(pageable);

        assertThat(result.getContent()).containsExactly(dto);
        verify(jobRepository).findAll(pageable);
    }

    @Test
    void saveJob_shouldSaveAndReturnDto() {
        JobRequestDto request = new JobRequestDto("job_1", "Développeur Java", "TechCorp", "Paris");
        Job entity = new Job();
        Job saved = new Job();
        JobDto dto = new JobDto("job_1", "Développeur Java", "TechCorp", "Paris");

        when(jobRepository.existsByExternalId("job_1")).thenReturn(false);
        when(jobMapper.toEntity(request)).thenReturn(entity);
        when(jobRepository.save(entity)).thenReturn(saved);
        when(jobMapper.toDto(saved)).thenReturn(dto);

        JobDto result = jobService.saveJob(request);

        assertThat(result).isSameAs(dto);
        verify(jobRepository).save(entity);
    }

    @Test
    void saveJob_shouldThrowConflictWhenExternalIdAlreadyExists() {
        JobRequestDto request = new JobRequestDto("job_1", "Développeur Java", "TechCorp", "Paris");
        when(jobRepository.existsByExternalId("job_1")).thenReturn(true);

        assertThatThrownBy(() -> jobService.saveJob(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("job_1");

        verify(jobRepository, never()).save(any());
    }

    @Test
    void deleteJob_shouldDeleteWhenExists() {
        when(jobRepository.existsById(1L)).thenReturn(true);

        jobService.deleteJob(1L);

        verify(jobRepository).deleteById(1L);
    }

    @Test
    void deleteJob_shouldThrowNotFoundWhenMissing() {
        when(jobRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> jobService.deleteJob(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(jobRepository, never()).deleteById(any());
    }
}