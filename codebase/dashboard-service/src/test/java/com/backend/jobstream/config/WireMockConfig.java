package com.backend.jobstream.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Configuration WireMock pour les tests d'intégration.
 * Démarre un serveur WireMock qui simule l'API Adzuna.
 */
@TestConfiguration
public class WireMockConfig {

    private static final Logger log = LoggerFactory.getLogger(WireMockConfig.class);
    private static WireMockServer wireMockServer;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Démarre WireMock une seule fois
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
            wireMockServer.start();
            
            // Configure les stubs pour l'API Adzuna
            configureStubs(wireMockServer);
        }
        
        // Enregistre le port dynamique pour Spring
        int port = wireMockServer.port();
        log.info("WireMock démarré sur le port: {}", port);
        
        // Rediriger les appels Adzuna vers WireMock
        registry.add("adzuna.api.base-url", () -> "http://localhost:" + port);
        registry.add("adzuna.api.app-id", () -> "test-app-id");
        registry.add("adzuna.api.app-key", () -> "test-app-key");
        
        log.info("Propriétés Adzuna configurées: base-url=http://localhost:{}", port);
    }

    private static void configureStubs(WireMockServer wireMockServer) {
        log.info("Configuration des stubs WireMock pour Adzuna...");
        
        // Stub pour recherche avec résultats - wildcard pour tout matcher
        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/v1/api/jobs/ch/search/1.*"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "results": [
                                    {
                                      "id": "job-001",
                                      "title": "Développeur Java",
                                      "company": "TechCorp",
                                      "location": {"display_name": "Paris, France"},
                                      "description": "Nous cherchons un développeur Java expérimenté",
                                      "salary_min": 50000,
                                      "salary_max": 80000,
                                      "contract_type": "permanent",
                                      "redirect_url": "https://example.com/job/1",
                                      "created": "2026-03-25T10:00:00Z"
                                    },
                                    {
                                      "id": "job-002",
                                      "title": "Full Stack Developer",
                                      "company": "WebAgency",
                                      "location": {"display_name": "Lyon, France"},
                                      "description": "Join our team as a Full Stack Developer",
                                      "salary_min": 45000,
                                      "salary_max": 75000,
                                      "contract_type": "permanent",
                                      "redirect_url": "https://example.com/job/2",
                                      "created": "2026-03-24T10:00:00Z"
                                    }
                                  ],
                                  "count": 2,
                                  "total": 50
                                }
                                """)));

        // Stub pour recherche sans résultats
        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/v1/api/jobs/ch/search/1.*"))
                .withQueryParam("what", WireMock.matching(".*invalide.*"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "results": [],
                                  "count": 0,
                                  "total": 0
                                }
                                """)));

        // Stub pour pagination - page 1
        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/v1/api/jobs/ch/search/1.*"))
                .withQueryParam("what", WireMock.equalTo("developpeur"))
                .withQueryParam("page", WireMock.equalTo("1"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "results": [
                                    {
                                      "id": "job-p1-001",
                                      "title": "Développeur Paris",
                                      "company": "Company A",
                                      "location": {"display_name": "Paris, France"},
                                      "description": "Développeur sur Paris",
                                      "redirect_url": "https://example.com/job/p1",
                                      "created": "2026-03-25T10:00:00Z"
                                    }
                                  ],
                                  "count": 1,
                                  "total": 20
                                }
                                """)));

        // Stub pour pagination - page 2
        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/v1/api/jobs/ch/search/2.*"))
                .withQueryParam("what", WireMock.equalTo("developpeur"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "results": [
                                    {
                                      "id": "job-p2-001",
                                      "title": "Développeur Lyon",
                                      "company": "Company B",
                                      "location": {"display_name": "Lyon, France"},
                                      "description": "Développeur sur Lyon",
                                      "redirect_url": "https://example.com/job/p2",
                                      "created": "2026-03-24T10:00:00Z"
                                    }
                                  ],
                                  "count": 1,
                                  "total": 20
                                }
                                """)));

        // Stub pour simuler une erreur serveur (500)
        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/v1/api/jobs/ch/search/1.*"))
                .withQueryParam("what", WireMock.equalTo("erreur500"))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "error": "Internal Server Error"
                                }
                                """)));
        
        log.info("Stubs WireMock pour Adzuna configurés avec succès");
    }
}
