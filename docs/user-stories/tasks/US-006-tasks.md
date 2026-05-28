# US-006: Supprimer une offre sauvegardée

## Objectif
Permettre au candidat de gérer sa liste d'offres sauvegardées en supprimant celles qui ne l'intéressent plus.

---

## Tâches

### Tâche 6.1: Backend - DELETE /api/saved-jobs/{id}
- **Description**: Endpoint de suppression
- **Type**: Backend
- **Estim.**: 1h
- **Dépendances**: 
  - openapi.yml (existant)
  - US-005 (Tâche 5.1)
- **Livrables**:
  - DELETE endpoint dans SavedJobController
  - Suppression en base
  - Réponse 204 No Content
- **Tests**:
  - Unit test
  - Test 404 si non trouvé

### Tâche 6.2: Frontend - Dialogue confirmation
- **Description**: Confirmation avant suppression
- **Type**: Frontend
- **Estim.**: 1h
- **Dépendances**: 
  - Tâche 6.1
- **Livrables**:
  - ConfirmDialogComponent (ou utilisation librairie)
  - Modal de confirmation
  - Bouton "Supprimer" sur chaque carte
- **Tests**:
  - Component test

### Tâche 6.3: Frontend - Suppression avec feedback
- **Description**: Suppression + toast + mise à jour liste
- **Type**: Frontend
- **Estim.**: 1h
- **Dépendances**: 
  - Tâche 6.2
- **Livrables**:
  - Toast "Offre supprimée"
  - Retirer l'offre de la liste (update state)
  - Gestion erreur
- **Tests**:
  - Component test
  - E2E test suppression

---

## Checklist Definition of Done

- [ ] Code review passé
- [ ] Tests unitaires >= 80% coverage
- [ ] Tests d'intégration passés
- [ ] E2E test passing
- [ ] Documentation mise à jour
