# US-009: Recevoir les mises à jour en temps réel (SSE)

## Objectif
Améliorer l'expérience utilisateur en montrant les mises à jour en temps réel sans recharger la page.

---

## Tâches

### Tâche 9.1: Backend - Endpoint SSE
- **Description**: Créer GET /api/events
- **Type**: Backend
- **Estim.**: 2h
- **Dépendances**: 
  - openapi.yml (existant)
- **Livrables**:
  - SseController avec endpoint /api/events
  - Connection gérée (SseEmitter)
  - Endpoint Heartbeat (keep-alive)
  - Gestion des connexions multiples
- **Tests**:
  - Unit test
  - Test connection SSE

### Tâche 9.2: Backend - Notification events
- **Description**: Envoyer les événements aux clients connectés
- **Type**: Backend
- **Estim.**: 1h
- **Dépendances**: 
  - Tâche 9.1
  - US-007, US-008 (analyses)
- **Livrables**:
  - Service de notification SSE
  - Types d'événements:
    - ANALYSIS_STARTED
    - ANALYSIS_PROGRESS
    - ANALYSIS_COMPLETED
    - ANALYSIS_FAILED
- **Tests**:
  - Unit test notification

### Tâche 9.3: Frontend - Service SSE
- **Description**: Gérer la connexion côté client
- **Type**: Frontend
- **Estim.**: 2h
- **Dépendances**: 
  - Tâche 9.1 (endpoint)
- **Livrables**:
  - SseService
  - Connexion EventSource
  - Retry automatique si déconnexion
  - Gestion des événements
  - Observable pour les mises à jour
- **Tests**:
  - Unit test (avec mock EventSource)

### Tâche 9.4: Frontend - Indicateur et notifications
- **Description**: UI pour montrer la connexion temps réel
- **Type**: Frontend
- **Estim.**: 2h
- **Dépendances**: 
  - Tâche 9.3
- **Livrables**:
  - SseIndicatorComponent (status: connected/disconnected)
  - Toast notifications pour:
    - Analyse terminée
    - Analyse échouée
  - Badge notification sur onglet browser (optionnel)
- **Tests**:
  - Component test

### Tâche 9.5: Frontend - Intégration avec les pages
- **Description**: Connecter SSE aux pages existantes
- **Type**: Frontend
- **Estim.**: 1h
- **Dépendances**: 
  - Tâche 9.4
- **Livrables**:
  - Connexion SSE sur page sauvegardées
  - Update automatique des statuts sur les cartes
  - Refresh liste après nouvelle analyse
- **Tests**:
  - E2E test

---

## Checklist Definition of Done

- [ ] Code review passé
- [ ] Tests unitaires >= 80% coverage
- [ ] Tests d'intégration passés
- [ ] E2E test passing
- [ ] Documentation mise à jour
