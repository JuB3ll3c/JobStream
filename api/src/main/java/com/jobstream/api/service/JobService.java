package com.jobstream.api.service;

import com.jobstream.api.exception.ResourceConflictException;
import com.jobstream.api.exception.ResourceNotFoundException;
import com.jobstream.api.mapper.JobMapper;
import com.jobstream.api.repository.JobRepository;
import com.jobstream.api.repository.UserRepository;
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
    private final UserRepository userRepository;

    public JobDto getJobById(Long id, Long userId){
        return jobRepository.findByIdAndUserId(id, userId)
                .map(jobMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    public Page<JobDto> getJobs(Long userId, Pageable pageable) {
        return jobRepository.findAllByUserId(userId, pageable)
                .map(jobMapper::toDto);
    }

    public JobDto getJobByExternalId(String externalId, Long userId) {
        return jobRepository.findByExternalIdAndUserId(externalId, userId)
                .map(jobMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with external id: " + externalId));
    }

    @Transactional
    public JobDto saveJob(Long userId, JobRequestDto jobRequestDto){
        if (jobRepository.existsByExternalIdAndUserId(jobRequestDto.getExternalId(), userId)) {
            throw new ResourceConflictException("Job already saved with external id: " + jobRequestDto.getExternalId());
        }
        var job = jobMapper.toEntity(jobRequestDto);
        job.setUser(userRepository.getReferenceById(userId));
        return jobMapper.toDto(jobRepository.save(job));
    }

    @Transactional
    public void deleteJob(Long id, Long userId){
        var job = jobRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        jobRepository.delete(job);
    }
}
