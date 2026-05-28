# US-008: Voir le résultat de l'analyse IA

## Objectif
Permettre au candidat de prendre une décision éclairée grâce à l'analyse IA.

---

## Tâches

### Tâche 8.1: Backend - GET /api/analyses/{id}
- **Description**: Endpoint pour récupérer le résultat
- **Type**: Backend
- **Estim.**: 1h
- **Dépendances**: 
  - openapi.yml (existant)
  - US-007 (Tâche 7.3 - analyse complétée)
- **Livrables**:
  - GET endpoint dans AnalysisController
  - Retourne AnalysisDto complet (score, summary, etc.)
- **Tests**:
  - Unit test
  - Test 404

### Tâche 8.2: AI Analyzer - Génération résultat complet
- **Description**: Métriques complètes (strengths, weaknesses, etc.)
- **Type**: Backend (AI Service)
- **Estim.**: 2h
- **Dépendances**: 
  - Tâche 7.3
- **Livrables**:
  - Génération du score (0-100)
  - Extraction des points forts
  - Extraction des points faibles
  - Recommandations personnalisées
  - Tags (technologies, remote, etc.)
  - Publication vers topic analyzed
- **Tests**:
  - Unit test analyse
  - Test qualité résultat

### Tâche 8.3: Backend - Dashboard consumer (SSE update)
- **Description: Consommer les analyses et notifier via SSE
- **Type**: Backend (Dashboard Service)
- **Estim.**: 2h
- **Dépendances**: 
  - Tâche 8.2
- **Livrables**:
  - Kafka consumer sur topic `job-offers-analyzed`
  - Update DB avec résultat analyse
  - Notification SSE vers frontend
- **Tests**:
  - Integration test

### Tâche 8.4: Frontend - Affichage résultat analyse
- **Description**: Carte et page de résultat
- **Type**: Frontend
- **Estim.**: 3h
- **Dépendances**: 
  - Tâche 8.1
- **Livrables**:
  - AnalysisResultComponent (page complète)
  - Score affiché (0-100, barre visuelle)
  - Résumé généré
  - Liste points forts
  - Liste points faibles
  - Recommandations
  - Tags
  - Status indicator
- **Tests**:
  - Component test

---

## Checklist Definition of Done

- [ ] Code review passé
- [ ] Tests unitaires >= 80% coverage
- [ ] Tests d'intégration passés
- [ ] E2E test passing
- [ ] Documentation mise à jour
