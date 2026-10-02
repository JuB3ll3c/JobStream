package com.jobstream.api.controller;

import com.jobstream.api.service.JobService;
import com.jobstream.api.entity.User;
import com.jobstream.dto.JobDto;
import com.jobstream.dto.JobRequestDto;
import com.jobstream.dto.PagedJobResponse;
import com.jobstream.endpoint.JobApi;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;

import static com.jobstream.api.utils.PageUtils.toPageable;
import static com.jobstream.api.utils.PageUtils.toPagedResponse;

@RestController
@RequiredArgsConstructor
public class JobController implements JobApi {
    private final JobService jobService;

    @Override
    public ResponseEntity<Void> deleteJob(Long id, @AuthenticationPrincipal User user) {
        jobService.deleteJob(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<JobDto> getJobById(Long id, @AuthenticationPrincipal User user) {
        JobDto job = jobService.getJobById(id, user.getId());
        return ResponseEntity.ok(job);
    }

    @Override
    public ResponseEntity<PagedJobResponse> getJobs(
            Integer page, Integer size, List<String> sort, @AuthenticationPrincipal User user) {
        Page<JobDto> jobDtoPage = jobService.getJobs(user.getId(), toPageable(page, size, sort));
        return ResponseEntity.ok(toPagedResponse(jobDtoPage));
    }

    @Override
    public ResponseEntity<JobDto> saveJob(JobRequestDto jobRequestDto, @AuthenticationPrincipal User user) {
        JobDto savedJob = jobService.saveJob(user.getId(), jobRequestDto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedJob.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedJob);
    }
}
