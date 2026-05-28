# ADR-010: Conception à 2 Topics Kafka (Raw/Analyzed)

## Status
**Accepted** - 2026-03-26

## Context

La communication entre services utilise Kafka. Nous devons définir la structure des topics.

### Problème
- Comment organiser les messages entre Dashboard et IA Analyzer ?
- Un seul topic ou plusieurs ?

## Decision

**2 Topics Kafka distincts**

```
┌────────────────┐      job-offers-raw       ┌────────────────┐
│   Dashboard    │ ───────────────────────▶ │  IA Analyzer   │
│   Producer     │                          │   Consumer     │
└────────────────┘                          └────────────────┘
                                                    │
                                                    ▼
                                            ┌────────────────┐
                                            │   Analyse     │
                                            │  (score,tags) │
                                            └────────────────┘
                                                    │
                                                    ▼
                                            job-offers-analyzed
                                                    │
                                                    ▼
┌────────────────┐                          ┌────────────────┐
│   Dashboard    │ ◀────────────────────────│  IA Analyzer  │
│   Consumer    │                          │   Producer    │
└────────────────┘                          └────────────────┘
```

| Topic | Partitions | Description |
|-------|------------|-------------|
| `job-offers-raw` | 3 | Jobs sauvegardés, en attente d'analyse |
| `job-offers-analyzed` | 3 | Jobs analysés, résultats prêts |

## Rationale

1. **Séparation claire**: Chaque topic a un rôle spécifique
2. **Découplage**: Le producer ne connaît pas le consumer
3. **Observabilité**: Plus facile de suivre le flux

## Consequences

### Positive
- ✅ Flux clair et traçable
- ✅ Possibilité d'ajouter d'autres consumers
- ✅ Facilite le debugging

### Negative
- ⚠️ Plus de topics à gérer

### Neutral
- ℹ️ DLT pour chaque topic (`job-offers-raw-dlt`, `job-offers-analyzed-dlt`)

## Alternatives Considered

### Alternative 1: Un seul topic
**Description**: Un seul topic pour tous les messages

**Pros**:
- Plus simple

**Cons**:
- Tout mélangé
- Difficile de différencier les messages

**Reason Rejected**: Moins maintainable

## Related Decisions
- ADR-002: Architecture Event-Driven avec Apache Kafka
- ADR-008: Kafka avec @RetryableTopic et DLT

## Change Log
- 2026-03-26: Initial decision (Status: Accepted)

---
**Decision Maker**: Architecture Team  
**Last Updated**: 2026-03-26