# US-005: Voir mes offres sauvegardées

## Objectif
Permettre au candidat d'accéder rapidement à sa collection d'offres intéressantes.

---

## Tâches

### Tâche 5.1: Backend - GET /api/saved-jobs paginé
- **Description**: Créer l'endpoint avec pagination
- **Type**: Backend
- **Estim.**: 2h
- **Dépendances**: 
  - openapi.yml (existant)
  - US-004 (Tâche 4.1)
- **Livrables**:
  - Endpoint avec Spring Data pagination
  - Tri (savedAt, title)
  - Filtres optionnels
- **Tests**:
  - Unit test pagination
  - Test performance

### Tâche 5.2: Frontend - Page Offres Sauvegardées
- **Description**: Créer la page complète
- **Type**: Frontend
- **Estim.**: 3h
- **Dépendances**: 
  - Tâche 5.1
- **Livrables**:
  - SavedJobsPageComponent
  - Route /saved-jobs
  - Liste des offres sauvegardées
  - Carte avec: titre, entreprise, lieu, date sauvegarde
  - Bouton Analyser par carte
  - Bouton Supprimer par carte
  - Compteur total ("X offres sauvegardées")
- **Tests**:
  - Component test
  - E2E test

### Tâche 5.3: Frontend - Tri et Empty State
- **Description**: Fonctionnalités supplémentaires
- **Type**: Frontend
- **Estim.**: 1h
- **Dépendances**: 
  - Tâche 5.2
- **Livrables**:
  - Tri par date (plus récent)
  - Tri par titre
  - Empty state (pas d'offres sauvegardées)
  - Illustration + message + CTA
- **Tests**:
  - Component test

---

## Checklist Definition of Done

- [ ] Code review passé
- [ ] Tests unitaires >= 80% coverage
- [ ] Tests d'intégration passés
- [ ] E2E test passing
- [ ] Documentation mise à jour
