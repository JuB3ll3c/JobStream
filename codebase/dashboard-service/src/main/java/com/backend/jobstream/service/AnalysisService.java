package com.backend.jobstream.service;

import com.backend.jobstream.entity.SavedJob;
import com.backend.jobstream.mapper.AnalysisMapper;
import com.backend.jobstream.repository.AnalysisRepository;
import com.backend.jobstream.repository.SavedJobRepository;
import com.backend.jobstream.util.UuidUtil;
import com.jobstream.dto.JobAnalysisRequest;
import com.jobstream.dto.JobAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Service for handling AI analysis operations
 */
@Service
public class AnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);

    private final AnalysisRepository analysisRepository;
    private final SavedJobRepository savedJobRepository;
    private final AnalysisMapper analysisMapper;
    private final KafkaTemplate<String, JobAnalysisRequest> kafkaTemplate;

    @Value("${app.kafka.topics.to-analyze}")
    private String topicToAnalyze;

    public AnalysisService(
            AnalysisRepository analysisRepository,
            SavedJobRepository savedJobRepository,
            AnalysisMapper analysisMapper,
            KafkaTemplate<String, JobAnalysisRequest> kafkaTemplate) {
        this.analysisRepository = analysisRepository;
        this.savedJobRepository = savedJobRepository;
        this.analysisMapper = analysisMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Triggers AI analysis for a saved job via Kafka
     *
     * @param id the UUID of the saved job
     * @throws ResponseStatusException if job not found or ID invalid
     */
    public void triggerAnalysis(String id) {
        log.info("Déclenchement de l'analyse IA pour le job: id={}", id);

        UUID uuid = UuidUtil.parseUuid(id);
        SavedJob savedJob = savedJobRepository.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Job sauvegardé non trouvé"));

        JobAnalysisRequest request = buildAnalysisRequest(savedJob);
        kafkaTemplate.send(topicToAnalyze, request);

        log.info("Message Kafka envoyé pour analyse: externalId={}", savedJob.getExternalId());
    }

    /**
     * Retrieves the AI analysis result for a saved job
     *
     * @param id the UUID of the saved job
     * @return JobAnalysisResult with analysis details
     * @throws ResponseStatusException if job or analysis not found, or ID invalid
     */
    public JobAnalysisResult getAnalysis(String id) {
        log.info("Récupération de l'analyse pour le job sauvegardé: id={}", id);

        UUID uuid = UuidUtil.parseUuid(id);

        SavedJob savedJob = savedJobRepository.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Job sauvegardé non trouvé"));

        var analysis = analysisRepository.findBySavedJob_Id(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Analyse non trouvée"));

        return analysisMapper.toDto(analysis, savedJob.getExternalId());
    }

    /**
     * Builds a Kafka message from a SavedJob entity
     */
    private JobAnalysisRequest buildAnalysisRequest(SavedJob savedJob) {
        JobAnalysisRequest request = new JobAnalysisRequest(
                savedJob.getExternalId(),
                savedJob.getTitle(),
                savedJob.getCompany());
        request.setLocation(savedJob.getLocation());
        request.setDescription(savedJob.getDescription());
        request.setRequirements(savedJob.getRequirements() != null ? 
                String.join(", ", savedJob.getRequirements()) : null);
        request.setBenefits(savedJob.getBenefits() != null ? 
                String.join(", ", savedJob.getBenefits()) : null);
        request.setContractType(savedJob.getContractType());
        request.setApplyUrl(savedJob.getJobUrl());

        return request;
    }
}
