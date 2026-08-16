package com.jobstream.api.service;

import com.jobstream.api.exception.ResourceConflictException;
import com.jobstream.api.exception.ResourceNotFoundException;
import com.jobstream.api.mapper.JobMapper;
import com.jobstream.api.repository.JobRepository;
import com.jobstream.dto.JobDto;
import com.jobstream.dto.JobRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobService {
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    public JobDto getJobById(Long id){
        return jobRepository.findById(id)
                .map(jobMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    public Page<JobDto> getJobs(Pageable pageable) {
        return jobRepository.findAll(pageable)
                .map(jobMapper::toDto);
    }

    @Transactional
    public JobDto saveJob(JobRequestDto jobRequestDto){
        if (jobRepository.existsByExternalId(jobRequestDto.getExternalId())) {
            throw new ResourceConflictException("Job already saved with external id: " + jobRequestDto.getExternalId());
        }
        return jobMapper.toDto(jobRepository.save(jobMapper.toEntity(jobRequestDto)));
    }

    @Transactional
    public void deleteJob(Long id){
        if (!jobRepository.existsById(id)) {
            throw new ResourceNotFoundException("Job not found with id: " + id);
        }
        jobRepository.deleteById(id);
    }
}
