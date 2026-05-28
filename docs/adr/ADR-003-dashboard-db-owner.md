# ADR-003: Le Dashboard Service Gère la Base de Données

## Status
**Accepted** - 2026-03-26

## Context

Après l'analyse IA, les résultats doivent être persistés. Deux options sont possibles:
1. Le IA Analyzer écrit directement en base
2. Le Dashboard Service gère la base de données

## Decision

**Le Dashboard Service est responsable de la base de données**

```
┌─────────────┐                         ┌─────────────────┐
│  Dashboard  │  Produce → job-offers   │ IA Analyzer     │
│  Service    │ ──────────────────────▶  │ Service         │
│  (DB Owner) │                         │                 │
└─────────────┘                         └────────┬────────┘
    │                                            │
    │                 job-offers-analyzed       │
    │◀───────────────────────────────────────────│
    │                                            │
    ▼                                            ▼
┌─────────────────┐                      ┌─────────────────┐
│  PostgreSQL     │◀─────────────────────│  Ne writ pas    │
│  (jobs, users,  │                      │  en DB          │
│   job_analysis) │                      └─────────────────┘
└─────────────────┘
```

## Rationale

1. **Single Responsibility**: Un seul service connaît le schéma de la DB
2. **Découplage**: Le service IA ne dépend pas de la DB
3. **Flexibilité**: Facile de changer la DB sans impacter le service IA
4. **Testabilité**: Plus facile à tester séparément

## Consequences

### Positive
- ✅ Séparation des responsabilités claire
- ✅ IA Analyzer indépendant de la DB
- ✅ Facile à changer de solution de stockage
- ✅ Schema centralisé

### Negative
- ⚠️ Le Dashboard doit gérer les writes pour l'analyse
- ⚠️ Plus de complexité dans le Dashboard (consumer + producer)

### Neutral
- ℹ️ Nécessite une bonne gestion des erreurs

## Alternatives Considered

### Alternative 1: IA Analyzer écrit directement
**Description**: Le service IA fait les inserts dans job_analysis

**Pros**:
- Plus direct

**Cons**:
- Couplage avec le schéma DB
- Deux services qui écritent
- Plus dur à tester

**Reason Rejected**: Violation du principe de responsabilité unique

## Related Decisions
- ADR-002: Architecture Event-Driven avec Apache Kafka
- ADR-010: Conception à 2 Topics Kafka

## Change Log
- 2026-03-26: Initial decision (Status: Accepted)

---
**Decision Maker**: Architecture Team  
**Last Updated**: 2026-03-26