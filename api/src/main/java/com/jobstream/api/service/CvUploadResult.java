package com.jobstream.api.service;

import com.jobstream.dto.CvMetadataDto;

public record CvUploadResult(CvMetadataDto metadata, boolean created) {}
