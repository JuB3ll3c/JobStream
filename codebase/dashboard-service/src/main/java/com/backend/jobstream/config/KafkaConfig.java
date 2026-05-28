package com.backend.jobstream.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic jobsToAnalyzeTopic() {
        return TopicBuilder.name("job-to-analyzed")
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic analyzedJobsTopic() {
        return TopicBuilder.name("analyzed-jobs")
                .partitions(3)
                .replicas(3)
                .build();
    }
}
