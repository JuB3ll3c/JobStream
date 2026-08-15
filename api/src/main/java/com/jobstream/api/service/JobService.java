package com.jobstream.api.service;

import com.jobstream.api.mapper.JobMapper;
import com.jobstream.api.repository.JobRepository;
import com.jobstream.dto.JobDto;
import com.jobstream.dto.JobRequestDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobService {
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    public JobDto getJobById(Long id){
        return jobRepository.findById(id)
                .map(jobMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Job non trouvé avec l'id : " + id));
    }

    public Page<JobDto> getJobs(Pageable pageable) {
        return jobRepository.findAll(pageable)
                .map(jobMapper::toDto);
    }

    @Transactional
    public JobDto saveJob(JobRequestDto jobDto){
        return jobMapper.toDto(jobRepository.save(jobMapper.toEntity(jobDto)));
    }

    @Transactional
    public void deleteJob(Long id){
        jobRepository.deleteById(id);
    }
}
