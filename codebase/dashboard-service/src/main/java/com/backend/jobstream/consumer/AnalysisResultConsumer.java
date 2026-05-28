package com.backend.jobstream.consumer;

import com.backend.jobstream.entity.Analysis;
import com.backend.jobstream.repository.AnalysisRepository;
import com.backend.jobstream.repository.SavedJobRepository;
import com.backend.jobstream.service.EventBroadcaster;
import com.jobstream.dto.JobAnalysisRequest;
import com.jobstream.dto.JobAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AnalysisResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnalysisResultConsumer.class);

    private final AnalysisRepository analysisRepository;
    private final SavedJobRepository savedJobRepository;
    private final EventBroadcaster eventBroadcaster;

    public AnalysisResultConsumer(
            AnalysisRepository analysisRepository,
            SavedJobRepository savedJobRepository,
            EventBroadcaster eventBroadcaster) {
        this.analysisRepository = analysisRepository;
        this.savedJobRepository = savedJobRepository;
        this.eventBroadcaster = eventBroadcaster;
    }

    @RetryableTopic(
            attempts = "3",                                  // 3 tentatives au total
            backOff = @BackOff(delay = 2000, multiplier = 2), // Attend 2s, puis 4s...
            autoCreateTopics = "true",                    // Spring crée les topics de retry
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = DltStrategy.FAIL_ON_ERROR,       // Si tout échoue -> Envoi en DLT
            replicationFactor = "3",
            numPartitions = "3"
    )
    @KafkaListener(topics = "${app.kafka.topics.analyzed}", groupId = "dashboard-group")
    public void consume(JobAnalysisResult result) {
        log.info("Réception du résultat d'analyse pour jobId: {}", result.getJobId());

        try {
            // Trouver le savedJob par externalId
            var savedJob = savedJobRepository.findByExternalId(result.getJobId())
                    .orElseThrow(() -> new RuntimeException("SavedJob non trouvé pour externalId: " + result.getJobId()));

            // Créer ou mettre à jour l'analyse
            Analysis analysis = analysisRepository.findBySavedJob_Id(savedJob.getId())
                    .orElseGet(() -> {
                        Analysis newAnalysis = new Analysis();
                        newAnalysis.setSavedJob(savedJob);
                        return newAnalysis;
                    });

            // Mapper les résultats
            analysis.setScore(result.getScore());
            analysis.setSummary(result.getSummary());
            analysis.setStrengths(result.getStrengths());
            analysis.setWeaknesses(result.getWeaknesses());
            analysis.setRecommendations(result.getRecommendations());
            analysis.setTags(result.getTags());

            if (result.getCompletedAt() != null) {
                analysis.setCompletedAt(result.getCompletedAt().toLocalDateTime());
            } else {
                analysis.setCompletedAt(LocalDateTime.now());
            }

            if (result.getErrorMessage() != null) {
                analysis.setErrorMessage(result.getErrorMessage());
            }

            analysisRepository.save(analysis);
            log.info("Analyse sauvegardée pour jobId: {}", result.getJobId());

            // Envoyer via SSE
            eventBroadcaster.broadcast(analysis);

        } catch (Exception e) {
            log.error("Erreur lors du traitement du résultat d'analyse: {}", e.getMessage(), e);
        }
    }

    @DltHandler
    public void handleDlt(JobAnalysisResult result, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Message envoyé en DLT après 3 échecs. Topic: {}, Offre ID: {}", topic, result.getJobId());
        // Méthode pour enregistrer l'erreur en base ou envoyer une alerte
    }
}