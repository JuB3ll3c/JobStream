# ADR-002: Architecture Event-Driven avec Apache Kafka

## Status
**Accepted** - 2026-03-26

## Context

JobStream est composé de deux services: Dashboard Service et IA Analyzer Service. Ces services doivent communiquer de manière asynchrone pour découpler les traitements.

### Problème
- Comment permettre au Dashboard Service de déclencher une analyse IA sans attendre ?
- Comment gérer la communication entre services de manière fiable ?
- Comment traiter les messages qui échouent ?

## Decision

**Architecture Event-Driven avec Apache Kafka**

```
┌─────────────┐     job-offers-raw      ┌─────────────────┐
│  Dashboard  │ ──────────────────────▶ │ IA Analyzer     │
│  Service    │                         │ Service         │
└─────────────┘                         └────────┬────────┘
                                                │
                                                ▼
                                  job-offers-analyzed
                                                │
                                                ▼
                                        ┌─────────────────┐
                                        │ Dashboard       │
                                        │ Service         │
                                        │ (Consumer)      │
                                        └─────────────────┘
```

## Rationale

1. **Découplage**: Le producer ne connaît pas le consumer
2. **Résilience**: Les messages persistent dans Kafka si un service est down
3. **Scalabilité**: Possibilité d'ajouter des consumers si besoin
4. **Asynchronisme**: Le Dashboard ne bloque pas en attente de l'analyse

## Consequences

### Positive
- ✅ Découplage fort entre services
- ✅ Résilience aux pannes
- ✅ Traitement asynchrone (meilleures perfs)
- ✅ Facile à étendre

### Negative
- ⚠️ Complexité supplémentaires (Kafka à maintenir)
- ⚠️ Latence pour l'analyse (pas instantanée)
- ⚠️ Monitoring nécessaire

### Neutral
- ℹ️ Need for Docker Compose pour le développement local

## Alternatives Considered

### Alternative 1: HTTP Synchrone
**Description**: Appeler le service IA directement via HTTP

**Pros**:
- Simple à mettre en place
- Réponse immédiate

**Cons**:
- Couplage fort
- Si IA service down, tout échoue
- Timeout possible

**Reason Rejected**: Pas assez résilient

### Alternative 2: File/Message Queue Simple
**Description**: Utiliser une file RabbitMQ ou une DB comme queue

**Pros**:
- Plus simple que Kafka

**Cons**:
- Moins de fonctionnalités
- Moins de support dans l'écosystème

**Reason Rejected**: Kafka est le standard dans l'écosystème Spring

## Related Decisions
- ADR-001: API-First avec OpenAPI
- ADR-008: Kafka avec @RetryableTopic et DLT
- ADR-010: Conception à 2 Topics Kafka

## References
- [Spring Kafka](https://spring.io/projects/spring-kafka)
- [Apache Kafka](https://kafka.apache.org/)

## Change Log
- 2026-03-26: Initial decision (Status: Accepted)

---
**Decision Maker**: Architecture Team  
**Last Updated**: 2026-03-26