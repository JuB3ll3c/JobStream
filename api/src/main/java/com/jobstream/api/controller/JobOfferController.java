package com.jobstream.api.controller;

import com.jobstream.api.service.AdzunaService;
import com.jobstream.dto.JobOfferSearchResponse;
import com.jobstream.endpoint.JobOfferApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JobOfferController implements JobOfferApi {
    private final AdzunaService adzunaService;

    @Override
    public ResponseEntity<JobOfferSearchResponse> searchJobOffers(
            String title, String location, Integer page, Integer size) {
        return ResponseEntity.ok(adzunaService.searchJobOffers(title, location, page, size));
    }
}
