package com.backend.jobstream.mapper;

import com.backend.jobstream.entity.Analysis;
import com.backend.jobstream.entity.SavedJob;
import com.jobstream.dto.AnalysisDto;
import com.jobstream.dto.SaveJobRequest;
import com.jobstream.dto.SavedJobDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Mapper pour convertir entre SavedJob (entity), SaveJobRequest (request DTO) et SavedJobDto (response DTO)
 */
@Component
public class SavedJobMapper {

    /**
     * Convertit SaveJobRequest en entité SavedJob
     */
    public SavedJob toEntity(SaveJobRequest request) {
        if (request == null) {
            return null;
        }

        SavedJob entity = new SavedJob();
        entity.setExternalId(request.getExternalId());
        entity.setTitle(request.getTitle());
        entity.setCompany(request.getCompany());
        entity.setLocation(request.getLocation());
        entity.setDescription(request.getDescription());
        entity.setContractType(request.getContractType());
        entity.setPostedDate(request.getPostedDate());
        entity.setJobUrl(request.getJobUrl());
        entity.setRequirements(request.getRequirements());
        entity.setBenefits(request.getBenefits());
        entity.setSavedAt(LocalDateTime.now());
        entity.setSourceApi("adzuna");

        return entity;
    }

    /**
     * Convertit entité SavedJob en SavedJobDto
     */
    public SavedJobDto toDto(SavedJob entity) {
        if (entity == null) {
            return null;
        }

        SavedJobDto dto = new SavedJobDto();
        dto.setId(entity.getId());
        dto.setExternalId(entity.getExternalId());
        dto.setTitle(entity.getTitle());
        dto.setCompany(entity.getCompany());
        dto.setLocation(entity.getLocation());
        dto.setDescription(entity.getDescription());
        
        dto.setJobUrl(entity.getJobUrl());
        dto.setRequirements(entity.getRequirements());
        dto.setBenefits(entity.getBenefits());
        
        // Set nullable fields directly
        dto.setSalaryMin(entity.getSalaryMin());
        dto.setSalaryMax(entity.getSalaryMax());
        dto.setContractType(entity.getContractType());
        dto.setPostedDate(entity.getPostedDate());
        
        // Convert LocalDateTime to OffsetDateTime
        if (entity.getSavedAt() != null) {
            OffsetDateTime offsetDateTime = entity.getSavedAt().atOffset(ZoneOffset.UTC);
            dto.setSavedAt(offsetDateTime);
        }
        
        // Map analysis if present
        if (entity.getAnalysis() != null) {
            Analysis analysis = entity.getAnalysis();
            AnalysisDto analysisDto = new AnalysisDto();
            analysisDto.setId(analysis.getId());
            analysisDto.setSavedJobId(entity.getId());
            analysisDto.setStatus(com.jobstream.dto.AnalysisStatus.COMPLETED);
            
            // Set values directly (no more JsonNullable)
            analysisDto.setScore(analysis.getScore());
            analysisDto.setSummary(analysis.getSummary());
            analysisDto.setStrengths(analysis.getStrengths());
            analysisDto.setWeaknesses(analysis.getWeaknesses());
            analysisDto.setRecommendations(analysis.getRecommendations());
            analysisDto.setTags(analysis.getTags());
            
            if (analysis.getCreatedAt() != null) {
                analysisDto.setCreatedAt(analysis.getCreatedAt().atOffset(ZoneOffset.UTC));
            }
            if (analysis.getCompletedAt() != null) {
                analysisDto.setCompletedAt(analysis.getCompletedAt().atOffset(ZoneOffset.UTC));
            }
            
            dto.setAnalysis(analysisDto);
        }
        
        return dto;
    }
}
