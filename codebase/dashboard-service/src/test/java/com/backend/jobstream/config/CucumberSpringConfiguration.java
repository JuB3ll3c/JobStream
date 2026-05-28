package com.backend.jobstream.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * Configuration Cucumber pour l'intégration avec Spring
 * Note: Les tests Cucumber sont désactivés temporairement car ils nécessitent des credentials API.
 * Pour les réactiver, configurez les variables d'environnement ADZUNA_APP_ID et ADZUNA_APP_KEY.
 */
@CucumberContextConfiguration
@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=none"
})
@ActiveProfiles("test")
@ContextConfiguration(classes = com.backend.jobstream.JobstreamApplication.class)
public class CucumberSpringConfiguration {
    // Configuration Cucumber désactivée temporairement
}
