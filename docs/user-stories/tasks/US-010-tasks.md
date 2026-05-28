# US-010: Vérifier la santé de l'API

## Objectif
Permettre de s'assurer que tous les services sont opérationnels.

---

## Tâches

### Tâche 10.1: Backend - Health endpoint
- **Description**: Créer GET /api/health
- **Type**: Backend
- **Estim.**: 1h
- **Dépendances**: 
  - openapi.yml (existant)
- **Livrables**:
  - HealthController avec /api/health
  - HealthResponse DTO
  - Vérifications:
    - Application OK
    - Database OK (optionnel)
    - Kafka OK (optionnel)
- **Tests**:
  - Unit test
  - Test actuator si utilisé

### Tâche 10.2: Frontend - Indicateur santé
- **Description**: Afficher le statut dans l'UI
- **Type**: Frontend
- **Estim.**: 1h
- **Dépendances**: 
  - Tâche 10.1
- **Livrables**:
  - HealthIndicatorComponent (footer ou header)
  - Vert si OK, rouge si problème
  - Polling periodic (toutes les 30s) ou check manuel
- **Tests**:
  - Component test

---

## Checklist Definition of Done

- [ ] Code review passé
- [ ] Tests unitaires >= 80% coverage
- [ ] Tests d'intégration passés
- [ ] E2E test passing
- [ ] Documentation mise à jour
