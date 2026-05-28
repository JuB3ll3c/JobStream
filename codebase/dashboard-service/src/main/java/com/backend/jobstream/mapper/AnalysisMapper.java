package com.backend.jobstream.mapper;

import com.backend.jobstream.entity.Analysis;
import com.jobstream.dto.JobAnalysisResult;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

/**
 * Mapper for converting Analysis entity to JobAnalysisResult DTO
 */
@Component
public class AnalysisMapper {

    /**
     * Converts Analysis entity to JobAnalysisResult DTO
     *
     * @param analysis the analysis entity
     * @param externalId the external ID of the job
     * @return JobAnalysisResult DTO
     */
    public JobAnalysisResult toDto(Analysis analysis, String externalId) {
        if (analysis == null) {
            return null;
        }

        JobAnalysisResult result = new JobAnalysisResult(externalId);
        result.setScore(analysis.getScore());
        result.setSummary(analysis.getSummary());
        result.setStrengths(analysis.getStrengths());
        result.setWeaknesses(analysis.getWeaknesses());
        result.setRecommendations(analysis.getRecommendations());
        result.setTags(analysis.getTags());
        result.setCreatedAt(analysis.getCreatedAt() != null ?
                analysis.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
        result.setCompletedAt(analysis.getCompletedAt() != null ?
                analysis.getCompletedAt().atOffset(ZoneOffset.UTC) : null);
        result.setErrorMessage(analysis.getErrorMessage());

        return result;
    }
}
