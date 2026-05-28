# US-004: Sauvegarder une offre

## Objectif
Permettre au candidat de conserver des offres qui l'intéressent pour comparaison ultérieure.

---

## Tâches

### Tâche 4.1: Backend - POST /api/saved-jobs
- **Description**: Créer l'endpoint de sauvegarde
- **Type**: Backend
- **Estim.**: 2h
- **Dépendances**: 
  - openapi.yml (existant)
- **Livrables**:
  - SavedJobController avec POST /api/saved-jobs
  - CreateSavedJobRequest DTO
  - SavedJobDto响应
  - Validation Bean (@Valid, contraintes)
  - Repository SavedJob
  - Service SavedJobService
- **Tests**:
  - Unit test controller
  - Test validation
  - Test conflit déjà sauvegardé (409)

### Tâche 4.2: Backend - Intégration Kafka (publish)
- **Description**: Publier vers topic Kafka lors de la sauvegarde
- **Type**: Backend
- **Estim.**: 2h
- **Dépendances**: 
  - Tâche 4.1
- **Livrables**:
  - KafkaProducerService
  - Publication vers topic `job-offers-raw`
  - Message avec les données du job
  - Gestion erreur publication
- **Tests**:
  - Unit test producer
  - Integration test Kafka

### Tâche 4.3: Frontend - Service SavedJobs
- **Description**: Créer le service Angular pour les jobs sauvegardés
- **Type**: Frontend
- **Estim.**: 1h
- **Dépendances**: 
  - Tâche 4.1 (API définie)
- **Livrables**:
  - SavedJobsService
  - Méthodes: getAll(), save(), delete()
  - Types générés
- **Tests**:
  - Unit test service

### Tâche 4.4: Frontend - Bouton Sauvegarder
- **Description**: Composant réutilisable pour sauvegarder
- **Type**: Frontend
- **Estim.**: 2h
- **Dépendances**: 
  - Tâche 4.3
- **Livrables**:
  - SaveJobButtonComponent
  - States: default, saved, loading, error
  - Confirmation visuelle (toast)
  - Message si déjà sauvegardé
- **Tests**:
  - Component test

---

## Checklist Definition of Done

- [ ] Code review passé
- [ ] Tests unitaires >= 80% coverage
- [ ] Tests d'intégration passés
- [ ] E2E test passing
- [ ] Documentation mise à jour
