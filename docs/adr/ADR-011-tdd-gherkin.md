# ADR-011: TDD avec Gherkin pour le Développement

## Status
**Accepted** - 2026-03-26

## Context

Nous voulons un développement guidée par les tests avec un workflow clair des user stories jusqu'à l'implémentation.

### Problème
- Comment formaliser les exigences avant de coder ?
- Comment s'assurer que le code correspond aux besoins ?
- Comment avoir une documentation executable ?

## Decision

**Workflow TDD avec Gherkin: US → Gherkin → TDD → Implémentation**

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  User       │    │  Feature    │    │  Tests      │    │  Code       │
│  Story      │───▶│  File       │───▶│  (Gherkin)  │───▶│  Implémenté │
│  (Jira)     │    │  (.feature) │    │  (Cucumber) │    │             │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

### Étapes du workflow

1. **User Story**: Rédaction de la US (en français, format standard)
2. **Gherkin**: Transformation en scénario Gherkin (Given/When/Then)
3. **TDD**: Écriture des tests qui échouent (Red)
4. **Implémentation**: Code qui fait passer les tests (Green)
5. **Refactor**: Nettoyage du code (Refactor)

### Exemple

**User Story:**
```
En tant qu'utilisateur, je veux rechercher des offres d'emploi par mot-clé, 
afin de trouver des opportunités correspondant à mon profil.
```

**Feature File (Gherkin):**
```gherkin
Feature: Recherche d'offres d'emploi

  Scenario: Recherche avec un mot-clé valide
    Given l'utilisateur est sur la page de recherche
    When l'utilisateur saisit "développeur java" dans le champ de recherche
    And l'utilisateur clique sur le bouton "Rechercher"
    Then une liste d'offres d'emploi s'affiche
    And chaque résultat contient "développeur" ou "java" dans le titre
```

**Test (Cucumber):**
```java
@Given("l'utilisateur est sur la page de recherche")
public void utilisateur_sur_page_recherche() {
    // Setup
}

@When("l'utilisateur saisit {string} dans le champ de recherche")
public void saisit_recherche(String motCle) {
    // Action
}

@Then("une liste d'offres d'emploi s'affiche")
public void liste_affichee() {
    // Assertion
}
```

## Rationale

1. **Documentation executable**: Les tests sont la documentation
2. **Collaboration**: Les scénarios sont compris par tous (métier, dev, test)
3. **Confiance**: Le code est validé par les tests
4. **Refactoring safe**: Les tests préviennent les régressions

## Consequences

### Positive
- ✅ Spécifications claires et validées
- ✅ Tests qui documentent le comportement
- ✅ Meilleure collaboration PO/Dev
- ✅ Refactoring en toute confiance

### Negative
- ⚠️ Temps initial pour écrire les scénarios
- ⚠️ Nécessite de la discipline
- ⚠️ Peut devenir lourd si mal utilisé

### Neutral
- ℹ️ Outils: Cucumber (Java), Cypress/Cucumber (Angular)
- ℹ️ Naming: feature files dans `src/test/resources/features`

## Alternatives Considered

### Alternative 1: Tests unitaires directement
**Description**: Écrire des tests sans formalisation préalable

**Pros**:
- Plus rapide au début
- Plus flexible

**Cons**:
- Pas de collaboration avec le PO
- Tests peuvent ne pas refléter les besoins réels

**Reason Rejected**: Risque de mal comprendre les besoins

## Implementation Notes

### Backend (Java/Spring)
- Framework: Cucumber JVM
- Placeholder: `src/test/resources/features/*.feature`
- Runner: `CucumberIntegrationTest.java`

### Frontend (Angular)
- Framework: Cypress avec cypress-cucumber-preprocessor
- Placeholder: `cypress/e2e/features/*.feature`
- Step definitions: `cypress/e2e/step_definitions/`

## Metrics for Success

- [ ] Chaque US a un fichier .feature
- [ ] Couverture de tests > 80%
- [ ] Tests passent avant merge

## Review Date
Review cette décision après **2 sprints** pour évaluer:
- Adoption par l'équipe
- Qualité des scénarios
- Temps de développement

## Related Decisions
- ADR-009: Testcontainers pour les Tests d'Intégration

## References
- [Cucumber](https://cucumber.io/)
- [Gherkin Syntax](https://cucumber.io/docs/gherkin/)

## Change Log
- 2026-03-26: Initial decision (Status: Accepted)

---
**Decision Maker**: Architecture Team  
**Stakeholders**: PO, Dev Team, QA
**Last Updated**: 2026-03-26