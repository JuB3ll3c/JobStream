# ADR-007: Intégration JSearch API (RapidAPI)

## Status
**Accepted** - 2026-03-26

## Context

Pour rechercher des offres d'emploi, nous avons besoin d'une source de données externe. Plusieurs options existent.

### Alternatives
1. **JSearch (RapidAPI)** - Agrégateur d'offres d'emploi
2. **Scraping personnalisé** - Plus de contrôle mais fragile
3. **LinkedIn/Indeed API** - Payant, complexe

## Decision

**JSearch API via RapidAPI**

```
┌─────────────┐                     ┌─────────────────┐
│  Dashboard  │ ──────────────────▶ │  JSearch API    │
│  Service    │   GET /search       │  rapidapi.com   │
└─────────────┘                     └─────────────────┘
```

**Configuration:**
```yaml
jsearch:
  api:
    base-url: https://jsearch.p.rapidapi.com
    host: jsearch.p.rapidapi.com
    key: ${RAPIDAPI_KEY}
```

## Rationale

1. **Prêt à l'emploi**: API déjà existante avec des millions d'offres
2. **Wrapper stable**: Gère le scraping et la normalisation
3. **Prix**: Offre gratuite avec quotas suffisants pour un prototype

## Consequences

### Positive
- ✅ Données rapidement disponibles
- ✅ Pas de maintenance de scraper
- ✅ Format JSON structuré
- ✅ Support de plusieurs pays

### Negative
- ⚠️ Dépendance externe (API peut changer)
- ⚠️ Limites de quotas (gratuit)
- ⚠️ Clé API nécessaire (variable d'environnement)

### Neutral
- ℹ️ May need to switch provider later

## Alternatives Considered

### Alternative 1: Scraping personnalisé
**Description**: Construire notre propre scraper Indeed/LinkedIn

**Pros**:
- Contrôle total
- Pas de dépendance externe

**Cons**:
- Fragile (les sites changent)
- Temps de développement
- Risque juridique

**Reason Rejected**: Trop risqué et chronophage

### Alternative 2: APIs officielles (LinkedIn, Indeed)
**Description**: Utiliser les APIs payantes

**Pros**:
- Stable
- Legal

**Cons**:
- Cher
- Processus d'approbation long

**Reason Rejected**: Pas adapté pour un prototype

## Implementation Notes

- Stocker la clé dans variable d'environnement `RAPIDAPI_KEY`
- Gérer les erreurs de manière élégante
- Mettre en cache les résultats si besoin

## Related Decisions
- ADR-001: API-First avec OpenAPI

## References
- [JSearch API](https://rapidapi.com/letscrape-6bRBa3QguO5/api/jsearch)

## Change Log
- 2026-03-26: Initial decision (Status: Accepted)

---
**Decision Maker**: Architecture Team  
**Last Updated**: 2026-03-26