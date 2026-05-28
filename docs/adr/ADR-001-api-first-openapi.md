# ADR-001: Approche API-First avec OpenAPI 3.0

## Status
**Accepted** - 2026-03-26

## Context

JobStream est un projet avec un frontend Angular et un backend Spring Boot. Nous devons définir comment les contrats API sont partagés entre les deux parties.

### Problème
- Comment garantir que le backend et le frontend utilisent les mêmes DTOs ?
- Comment éviter les désynchronisations entre les équipes ?
- Comment générer automatiquement le code à partir des spécifications ?

### Alternatives considérées
1. **Manuelle**: Créer les DTOs à la main dans chaque projet
2. **Code First**: Écrire le code, puis générer la spec OpenAPI
3. **API-First**: Définir la spec OpenAPI en premier, puis générer le code

## Decision

**API-First avec OpenAPI 3.0**

Le fichier `openapi/openapi.yml` est la **source de vérité** pour tous les contrats API.

```
openapi/openapi.yml  ───▶  DTOs Backend (Java)
        │
        └──▶  Client Frontend (TypeScript)
```

### Implémentation

**Backend** (Maven):
```xml
<plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <version>7.0.1</version>
    <configuration>
        <inputSpec>${project.basedir}/../openapi/openapi.yml</inputSpec>
        <generatorName>spring</generatorName>
        <modelPackage>com.jobstream.api.dto</modelPackage>
    </configuration>
</plugin>
```

**Frontend** (npm):
```bash
npx openapi-generator-cli generate \
  -i ../openapi/openapi.yml \
  -g typescript-axios \
  -o src/app/api/client
```

## Rationale

1. **Single Source of Truth**: Un seul fichier pour les deux équipes
2. **Génération automatique**: Plus de copier-coller manuel
3. **Validation**: La spec peut être validée avant le développement
4. **Documentation**: Swagger UI généré automatiquement
5. **Réversibilité**: Facile à modifier et regénérer

## Consequences

### Positive
- ✅ DTOs toujours synchronisés entre backend et frontend
- ✅ Gain de temps sur le développement
- ✅ Documentation toujours à jour
- ✅ Validation des contrats en CI/CD

### Negative
- ⚠️ Besoin d'installer les outils de génération
- ⚠️ Structure des DTOs imposée par la spec

### Neutral
- ℹ️ Formation nécessaire pour l'équipe
- ℹ️ Workflow de développement slightly plus long (gen + dev)

## Alternatives Considered

### Alternative 1: DTOs Manuels
**Description**: Créer les DTOs à la main dans chaque projet

**Pros**:
- Contrôle total sur le code
- Simple au départ

**Cons**:
- Désynchronisation fréquente
- Duplication de code
- Maintenance fastidieuse

**Reason Rejected**: Non évolutif, risque d'erreurs

### Alternative 2: Code First
**Description**: Écrire le code Java, générer la spec OpenAPI

**Pros**:
- Plus rapide pour les développeurs backend

**Cons**:
- Le frontend doit attendre que le backend soit développé
- Pas de contrat visible avant l'implémentation

**Reason Rejected**: Contre le principe de collaboration early

## Implementation Notes

1. Tout nouveau DTO doit être ajouté dans `openapi/openapi.yml`
2. Exécuter `mvn generate-sources` après modification de la spec
3. Pour le frontend: script `npm run generate-api` dans package.json
4. **NE JAMAIS modifier les fichiers générés manuellement**

## Metrics for Success

- [ ] 100% des endpoints définis dans OpenAPI
- [ ] Zéro DTO créé manuellement
- [ ] Génération automatique en CI/CD
- [ ] Documentation Swagger accessible

## Review Date
Review cette décision dans **3 mois** (Juin 2026) pour évaluer:
- Adoption par l'équipe
- Qualité des DTOs générés
- Temps économisé

## Related Decisions
- ADR-002: Architecture Event-Driven avec Apache Kafka
- ADR-006: Angular Signals pour la Gestion d'État
- ADR-008: Kafka avec @RetryableTopic et DLT

## References
- [OpenAPI 3.0 Specification](https://spec.openapis.org/oas/v3.0.3)
- [OpenAPI Generator](https://openapi-generator.tech/)

## Change Log
- 2026-03-26: Initial decision (Status: Accepted)

---
**Decision Maker**: Architecture Team  
**Stakeholders**: Backend Team, Frontend Team  
**Last Updated**: 2026-03-26