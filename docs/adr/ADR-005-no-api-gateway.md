# ADR-005: Pas d'API Gateway (Prototype)

## Status
**Accepted** - 2026-03-26

## Context

Pour un prototype, devons-nous ajouter une API Gateway pour router les requêtes ?

### Alternatives
1. **Sans Gateway**: Frontend appelle directement les services
2. **Avec Gateway**: API Gateway centralise les requêtes

## Decision

**Pas d'API Gateway pour le prototype**

```
┌─────────────┐                     ┌─────────────────┐
│   Frontend  │ ──────────────────▶ │ Dashboard       │
│  (Angular)  │                     │ Service :8080   │
└─────────────┘                     └─────────────────┘
       │
       │ (pas de gateway)
       │
       ▼
┌─────────────────┐
│ IA Analyzer     │
│ Service :8081   │
│ (pas accessible │
│  par frontend) │
└─────────────────┘
```

## Rationale

1. **Simplicité**: Moins de composants à déployer
2. **Coût**: Gratuit (pas de serveur supplémentaire)
3. ** Suffisant**: Le frontend n'a besoin que du Dashboard Service

## Consequences

### Positive
- ✅ Plus simple à déployer
- ✅ moins de configuration
- ✅ Pas de point de défaillance unique

### Negative
- ⚠️ Pas de centralisation du monitoring
- ⚠️ Pas de rate limiting
- ⚠️ Frontend doit connaître les URLs des services

### Neutral
- ℹ️ À ajouter si le projet grandit

## Alternatives Considered

### Alternative 1: Avec API Gateway (Spring Cloud Gateway)
**Description**: Ajouter une gateway pour router vers les services

**Pros**:
- Routing centralisé
- Rate limiting
- Monitoring

**Cons**:
- Plus de complexité
- Plus de ressources

**Reason Rejected**: Inutile pour un prototype

## Review Date
Review cette décision si le projet évolue vers une architecture plus complexe.

## Related Decisions
- ADR-002: Architecture Event-Driven avec Apache Kafka

## Change Log
- 2026-03-26: Initial decision (Status: Accepted)

---
**Decision Maker**: Architecture Team  
**Last Updated**: 2026-03-26