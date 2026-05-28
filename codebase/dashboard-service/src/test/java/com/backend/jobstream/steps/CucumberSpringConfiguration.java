package com.backend.jobstream.steps;

import com.backend.jobstream.JobstreamApplication;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.spring.CucumberContextConfiguration;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@CucumberContextConfiguration
@SpringBootTest(classes = JobstreamApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CucumberSpringConfiguration.class);
    private static WireMockServer wireMockServer;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Démarre WireMock une seule fois
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
            wireMockServer.start();
            
            // Configure les stubs pour l'API JSearch
            configureStubs(wireMockServer);
        }
        
        // Enregistre le port dynamique pour Spring
        int port = wireMockServer.port();
        log.info("WireMock démarré sur le port: {}", port);
        
        registry.add("jsearch.api.base-url", () -> "http://localhost:" + port);
        registry.add("jsearch.api.key", () -> "test-api-key");
        registry.add("jsearch.api.host", () -> "localhost");
        
        log.info("Propriétés JSearch configurées: base-url=http://localhost:{}, host=localhost", port);
    }

    private static void configureStubs(WireMockServer wireMockServer) {
        log.info("Configuration des stubs WireMock...");
        
        // Stub pour recherche avec résultats
        wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/search"))
                .withQueryParam("query", WireMock.matching(".*"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "status": "OK",
                                  "request_id": "test-123",
                                  "data": [
                                    {
                                      "job_id": "job-001",
                                      "job_title": "Développeur Java",
                                      "employer_name": "TechCorp",
                                      "job_city": "Paris",
                                      "job_description": "Nous cherchons un développeur Java expérimenté",
                                      "job_apply_link": "https://example.com/job/1",
                                      "job_employment_type": "FULLTIME",
                                      "job_salary_range": {
                                        "min": 50000,
                                        "max": 80000
                                      },
                                      "job_required_skills": ["Java 17", "Spring Boot", "PostgreSQL"],
                                      "job_benefits": ["RTT", "Télétravail"]
                                    },
                                    {
                                      "job_id": "job-002",
                                      "job_title": "Full Stack Developer",
                                      "employer_name": "WebAgency",
                                      "job_city": "Lyon",
                                      "job_description": "Join our team as a Full Stack Developer",
                                      "job_apply_link": "https://example.com/job/2",
                                      "job_employment_type": "FULLTIME",
                                      "job_salary_range": {
                                        "min": 45000,
                                        "max": 75000
                                      },
                                      "job_required_skills": ["React", "Node.js", "TypeScript"],
                                      "job_benefits": ["Mutuelle", "Tickets restaurant"]
                                    }
                                  ]
                                }
                                """)));

        // Stub pour recherche sans résultats
        wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/search"))
                .withQueryParam("query", WireMock.matching(".*invalide.*"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "status": "OK",
                                  "request_id": "test-456",
                                  "data": []
                                }
                                """)));
        
        log.info("Stubs WireMock configurés avec succès");
    }

    @PreDestroy
    public void stopWireMock() {
        if (wireMockServer != null) {
            log.info("Arrêt de WireMock...");
            wireMockServer.stop();
        }
    }
}
