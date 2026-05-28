# ADR-009: Testcontainers pour les Tests d'Intégration

## Status
**Accepted** - 2026-03-26

## Context

Nous avons besoin de tests d'intégration qui utilisent une vraie base de données PostgreSQL et un vrai broker Kafka.

### Problème
- Comment tester le code qui utilise Kafka et PostgreSQL ?
- Faut-il utiliser des mocks ou des vrais services ?

## Decision

**Testcontainers pour les tests d'intégration**

```java
@Testcontainers
class JobServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("jobstream")
        .withUsername("test")
        .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @Test
    void shouldSaveJob() {
        // Test avec vraie DB et vrai Kafka
    }
}
```

## Rationale

1. **Isolation**: Chaque test a sa propre base/Kafka
2. **Réalisme**: Test avec les vraies technologies
3. **CI/CD**: Fonctionne dans GitHub Actions

## Consequences

### Positive
- ✅ Tests réalistes avec vrais services
- ✅ Isolation entre tests
- ✅ fonctionne en CI/CD

### Negative
- ⚠️ Plus lent que les tests unitaires
- ⚠️ Nécessite Docker
- ⚠️ Plus de ressources

### Neutral
- ℹ️ Utiliser pour les tests d'intégration, pas unitaires

## Alternatives Considered

### Alternative 1: Mocks (Mockito, EmbeddedKafka)
**Description**: Utiliser des mocks pour Kafka et DB

**Pros**:
- Plus rapide
- Pas de Docker

**Cons**:
- Ne teste pas l'intégration réelle
- Peut cacher des bugs

**Reason Rejected**: Pas assez réaliste pour la production

## Metrics for Success

- [ ] Tests d'intégration avec Testcontainers passent
- [ ] Temps d'exécution < 5 min

## Related Decisions
- ADR-002: Architecture Event-Driven avec Apache Kafka

## References
- [Testcontainers](https://www.testcontainers.org/)

## Change Log
- 2026-03-26: Initial decision (Status: Accepted)

---
**Decision Maker**: Architecture Team  
**Last Updated**: 2026-03-26