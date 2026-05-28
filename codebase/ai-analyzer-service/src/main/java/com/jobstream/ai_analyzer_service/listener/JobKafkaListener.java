package com.jobstream.ai_analyzer_service.listener;

import com.jobstream.ai_analyzer_service.service.JobProcessingService;
import com.jobstream.dto.JobAnalysisRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JobKafkaListener {
    private final JobProcessingService jobProcessingService;

    public JobKafkaListener(JobProcessingService jobProcessingService) {
        this.jobProcessingService = jobProcessingService;
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
    @KafkaListener(topics = "${app.kafka.topics.consumer-topic}")
    public void listen(JobAnalysisRequest offer) {
        log.info("Message reçu : {}", offer.getJobTitle() + " - " + offer.getDescription());
        jobProcessingService.processIncomingJob(offer);
    }

    @DltHandler
    public void handleDlt(JobAnalysisRequest offer, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Message envoyé en DLT après 3 échecs. Topic: {}, Offre ID: {}", topic, offer.getJobId());
        // Méthode pour enregistrer l'erreur en base ou envoyer une alerte
    }
}
