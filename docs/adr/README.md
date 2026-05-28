# Architecture Decision Records

Ce répertoire contient tous les Architecture Decision Records (ADRs) pour le projet JobStream.

## Qu'est-ce qu'un ADR ?

Les Architecture Decision Records documentent les décisions architecturales importantes prises pendant le développement du projet. Chaque ADR décrit :
- Le contexte et le problème
- La décision prise
- Les conséquences (avantages et inconvénients)
- Les alternatives considérées

## Liste des ADRs

| # | Titre | Status | Date |
|---|-------|--------|------|
| [ADR-001](./ADR-001-api-first-openapi.md) | Approche API-First avec OpenAPI 3.0 | ✅ Accepted | 2026-03-26 |
| [ADR-002](./ADR-002-event-driven-kafka.md) | Architecture Event-Driven avec Apache Kafka | ✅ Accepted | 2026-03-26 |
| [ADR-003](./ADR-003-dashboard-db-owner.md) | Le Dashboard Service gère la Base de Données | ✅ Accepted | 2026-03-26 |
| [ADR-004](./ADR-004-sse-real-time.md) | Server-Sent Events (SSE) pour le Temps Réel | ✅ Accepted | 2026-03-26 |
| [ADR-005](./ADR-005-no-api-gateway.md) | Pas d'API Gateway (Prototype) | ✅ Accepted | 2026-03-26 |
| [ADR-006](./ADR-006-angular-signals.md) | Angular Signals pour la Gestion d'État | ✅ Accepted | 2026-03-26 |
| [ADR-007](./ADR-007-jsearch-api.md) | Intégration JSearch API (RapidAPI) | ✅ Accepted | 2026-03-26 |
| [ADR-008](./ADR-008-kafka-retry-dlt.md) | Kafka avec @RetryableTopic et DLT | ✅ Accepted | 2026-03-26 |
| [ADR-009](./ADR-009-testcontainers.md) | Testcontainers pour les Tests d'Intégration | ✅ Accepted | 2026-03-26 |
| [ADR-010](./ADR-010-two-topics-kafka.md) | Conception à 2 Topics Kafka (Raw/Analyzed) | ✅ Accepted | 2026-03-26 |
| [ADR-011](./ADR-011-tdd-gherkin.md) | TDD avec Gherkin pour le Développement | ✅ Accepted | 2026-03-26 |

## Légende des Status

- ✅ **Accepted**: Décision approuvée et en cours d'implémentation
- 📝 **Proposed**: Décision en cours de réflexion
- ❌ **Rejected**: Décision examinée mais rejetée
- ⚠️ **Deprecated**: Décision dépassée
- 🔄 **Superseded**: Décision remplacée par une nouvelle

## Workflow de création

1. **Proposer**: Créer un ADR avec le status "proposed"
2. **Discuter**: Revoir avec l'équipe et les parties prenantes
3. **Décider**: Mettre à jour le status vers "accepted" ou "rejected"
4. **Implémenter**: Exécuter la décision
5. **Revoir**: Réviser périodiquement les décisions

## Structure d'un ADR

```markdown
# ADR-XXX: [Titre]

## Status
[Proposed/Accepted/Rejected] - [Date]

## Context
[Description du problème/背景]

## Decision
[Description de la décision]

## Rationale
[Raisons du choix]

## Consequences
### Positive
### Negative
### Neutral

## Alternatives Considered
[Autres options envisagées]

## Implementation Notes
[Notes d'implémentation]
```

## Pour créer un nouvel ADR

Copier le fichier `template.md` et le remplir avec les informations appropriées.

## Réferences

- [Markdown ADR Template](https://github.com/joelparkerhenderson/architecture-decision-record)
- [Cucumber](https://cucumber.io/) - Pour ADR-011