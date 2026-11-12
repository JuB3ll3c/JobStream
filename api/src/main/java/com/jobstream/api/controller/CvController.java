package com.jobstream.api.controller;

import com.jobstream.api.entity.User;
import com.jobstream.api.service.CvService;
import com.jobstream.dto.CvMetadataDto;
import com.jobstream.endpoint.CvApi;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
public class CvController implements CvApi {
    private final CvService cvService;

    @Override
    public ResponseEntity<CvMetadataDto> uploadCv(MultipartFile file, @AuthenticationPrincipal User user) {
        var result = cvService.uploadCv(user.getId(), file);
        if (result.created()) {
            return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().build().toUri())
                    .body(result.metadata());
        }
        return ResponseEntity.ok(result.metadata());
    }

    @Override
    public ResponseEntity<CvMetadataDto> getCvMetadata(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cvService.getCvMetadata(user.getId()));
    }

    @Override
    public ResponseEntity<Resource> downloadCv(@AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Override
    public ResponseEntity<Void> deleteCv(@AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
