package com.jobstream.ai_analyzer_service.service;

import com.jobstream.ai_analyzer_service.exception.AiAnalysisException;
import com.jobstream.dto.JobAnalysisRequest;
import com.jobstream.dto.JobAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JobProcessingService {
    @Value("${app.kafka.topics.producer-topic}")
    private String topicAnalyzedJobs;
    
    private final AiAnalyzerService aiAnalyzerService;
    private final KafkaTemplate<String, JobAnalysisResult> kafkaTemplate;

    public JobProcessingService(AiAnalyzerService aiAnalyzerService, 
                                  KafkaTemplate<String, JobAnalysisResult> kafkaTemplate) {
        this.aiAnalyzerService = aiAnalyzerService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void processIncomingJob(JobAnalysisRequest jobAnalysisRequest) {
        String jobId = jobAnalysisRequest.getJobId();
        
        try {
            log.info("Début de l'analyse pour le job: {}", jobId);
            var analysis = aiAnalyzerService.analyze(jobAnalysisRequest.getDescription());
            analysis.setJobId(jobId);
            
            kafkaTemplate.send(topicAnalyzedJobs, analysis);
            log.info("Analyse terminée et envoyée pour le job: {}", jobId);
        } catch (AiAnalysisException e) {
            log.error("Erreur lors de l'analyse IA pour le job {}: {}", jobId, e.getMessage(), e);
            throw e; // Re-throw pour que le retry mechanism de Kafka gère
        } catch (Exception e) {
            log.error("Erreur inattendue lors du traitement du job {}: {}", jobId, e.getMessage(), e);
            throw new RuntimeException("Erreur lors du traitement du job", e);
        }
    }
}
