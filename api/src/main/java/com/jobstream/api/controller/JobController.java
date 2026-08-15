package com.jobstream.api.controller;

import com.jobstream.api.service.JobService;
import com.jobstream.dto.JobDto;
import com.jobstream.dto.JobRequestDto;
import com.jobstream.dto.PagedJobResponse;
import com.jobstream.endpoint.JobApi;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import static com.jobstream.api.utils.PageUtils.toPagedResponse;

@RestController
@RequiredArgsConstructor
public class JobController implements JobApi {
    private final JobService jobService;

    @Override
    public ResponseEntity<Void> deleteJob(Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<JobDto> getJobById(Long id) {
        JobDto job = jobService.getJobById(id);
        return ResponseEntity.ok(job);
    }

    @Override
    public ResponseEntity<PagedJobResponse> getJobs(Pageable pageable) {
        Page<JobDto> jobDtoPage = jobService.getJobs(pageable);
        return ResponseEntity.ok(toPagedResponse(jobDtoPage));
    }

    @Override
    public ResponseEntity<JobDto> saveJob(JobRequestDto jobRequestDto) {
        JobDto savedJob = jobService.saveJob(jobRequestDto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedJob.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedJob);
    }
}
