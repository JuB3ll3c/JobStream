# ADR-008: Kafka avec @RetryableTopic et DLT

## Status
**Accepted** - 2026-03-26

## Context

Quand un message échoue dans Kafka, nous devons gérer les retries et les messages qui échouent définitivement.

### Problème
- Comment gérer les messages qui échouent temporairement ?
- Comment éviter de perdre les messages ?
- Comment identifier les messages qui ne peuvent pas être traités ?

## Decision

**Spring Kafka avec @RetryableTopic et Dead Letter Topic**

```java
@RetryableTopic(
    attempts = "4",
    backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
    dltTopicSuffix = "-dlt"
)
@KafkaListener(topics = "job-offers-raw", groupId = "ia-analyzer-group")
public void consume(ConsumerRecord<String, JobOfferEvent> record) {
    try {
        processJobOffer(record.value());
    } catch (Exception e) {
        throw e; // Permet à @RetryableTopic de gérer
    }
}

@DltHandler
public void handleDlt(ConsumerRecord<String, JobOfferEvent> record) {
    log.error("Message sent to DLT: {}", record.value());
    // Alerter, logger pour investigation
}
```

## Configuration

```yaml
spring:
  kafka:
    producer:
      acks: all
      enable-idempotence: true
      properties:
        retries:
          max: 3
```

## Rationale

1. **Résilience automatique**: Les retries sont gérés par Spring
2. **DLT pour les échecs**: Les messages qui échouent vont dans un topic spécifique
3. **Backoff exponentiel**: Évite de surcharger le système

## Consequences

### Positive
- ✅ Auto-retry avec backoff
- ✅ Messages failures isolés dans DLT
- ✅ Pas de perte de messages
- ✅ Monitoring facilité

### Negative
- ⚠️ Latence pour les messages qui retry
- ⚠️ DLT doit être monitoré

### Neutral
- ℹ️ Topic DLT nommé `job-offers-raw-dlt`

## Alternatives Considered

### Alternative 1: Retry manuel
**Description**: Gérer les retries manuellement dans le code

**Pros**:
- Plus de contrôle

**Cons**:
- Plus de code
- Facile de faire des erreurs

**Reason Rejected**: Plus complexe, moins réutilisable

## Metrics for Success

- [ ] Les messages failed goes to DLT
- [ ] Backoff exponentiel fonctionne
- [ ] Idempotence activée

## Related Decisions
- ADR-002: Architecture Event-Driven avec Apache Kafka

## References
- [Spring Kafka RetryableTopic](https://docs.spring.io/spring-kafka/reference/#retry-topic)

## Change Log
- 2026-03-26: Initial decision (Status: Accepted)

---
**Decision Maker**: Architecture Team  
**Last Updated**: 2026-03-26