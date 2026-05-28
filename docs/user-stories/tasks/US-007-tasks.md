# US-007: Déclencher l'analyse IA d'une offre

## Objectif
Permettre au candidat d'avoir une analyse objective et détaillée de chaque offre via l'IA.

---

## Tâches

### Tâche 7.1: Backend - POST /api/analyses
- **Description**: Endpoint pour déclencher l'analyse
- **Type**: Backend
- **Estim.**: 2h
- **Dépendances**: 
  - openapi.yml (existant)
- **Livrables**:
  - AnalysisController avec POST /api/analyses
  - CreateAnalysisRequest DTO
  - AnalysisDto réponse (avec status PENDING)
  - Analyse entity en base (status PENDING)
  - Réponse 202 Accepted
- **Tests**:
  - Unit test controller
  - Test création analyse

### Tâche 7.2: Backend - Kafka publish (raw topic)
- **Description**: Publier vers job-offers-raw
- **Type**: Backend
- **Estim.**: 1h
- **Dépendances**: 
  - Tâche 7.1
- **Livrables**:
  - Kafka producer pour analyse
  - Message vers topic `job-offers-raw`
- **Tests**:
  - Unit test
  - Integration test

### Tâche 7.3: AI Analyzer - Consumer Kafka
- **Description**: Consumer qui traite les jobs bruts
- **Type**: Backend (AI Service)
- **Estim.**: 3h
- **Dépendances**: 
  - Tâche 7.2
- **Livrables**:
  - Kafka consumer sur topic `job-offers-raw`
  - Logique d'analyse IA (mock pour prototype)
  - Update status: PENDING → IN_PROGRESS
  - Publication vers topic `job-offers-analyzed`
- **Tests**:
  - Unit test consumer
  - Integration test Kafka

### Tâche 7.4: Frontend - Bouton Analyser
- **Description**: Créer le composant et integrate
- **Type**: Frontend
- **Estim.**: 2h
- **Dépendances**: 
  - Tâche 7.1 (API)
- **Livrables**:
  - AnalyzeButtonComponent
  - États: idle, loading, completed, error
  - Intégration dans page sauvegardées
- **Tests**:
  - Component test

### Tâche 7.5: Frontend - Bouton "Analyser tout"
- **Description**: Analyse batch de toutes les offres
- **Type**: Frontend
- **Estim.**: 1h
- **Dépendances**: 
  - Tâche 7.4
- **Livrables**:
  - Bouton "Analyser tout" dans toolbar
  - Logique batch (appels multiples ou endpoint batch)
  - Progress indicator
- **Tests**:
  - Component test

---

## Checklist Definition of Done

- [ ] Code review passé
- [ ] Tests unitaires >= 80% coverage
- [ ] Tests d'intégration passés
- [ ] E2E test passing
- [ ] Documentation mise à jour
