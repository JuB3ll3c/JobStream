package com.jobstream.api.controller;

import com.jobstream.api.service.AdzunaService;
import com.jobstream.dto.JobDto;
import com.jobstream.endpoint.AdzunaApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AdzunaController implements AdzunaApi {

    private final AdzunaService adzunaService;

    @Override
    public ResponseEntity<JobDto> getAdzunaJobById(String externalId) {
        return ResponseEntity.ok(adzunaService.getJobById(externalId));
    }
}
