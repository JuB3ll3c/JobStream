# US-003: Voir les détails d'une offre

## Objectif
Permettre au candidat de voir toutes les informations détaillées d'une offre d'emploi.

---

## Tâches

### Tâche 3.1: Backend - Endpoint détail job externe
- **Description**: Créer GET /api/jobs/{externalId}
- **Type**: Backend
- **Estim.**: 1h
- **Dépendances**: 
  - openapi.yml (existant)
- **Livrables**:
  - Ajout endpoint dans JobSearchController
  - Cache ou appel JSearch selon stratégie
- **Tests**:
  - Unit test endpoint
  - Test erreur 404

### Tâche 3.2: Backend - Endpoint détail job sauvegardé
- **Description**: Créer GET /api/saved-jobs/{id}
- **Type**: Backend
- **Estim.**: 1h
- **Dépendances**: 
  - openapi.yml (existant)
- **Livrables**:
  - SavedJobController avec endpoint
  - Repository SavedJob
- **Tests**:
  - Unit test controller
  - Test intégration DB

### Tâche 3.3: Frontend - Page détail
- **Description**: Créer la page de détails complète
- **Type**: Frontend
- **Estim.**: 3h
- **Dépendances**: 
  - Tâche 3.1 ou 3.2
- **Livrables**:
  - JobDetailComponent (page)
  - Route /jobs/:id et /saved-jobs/:id
  - Affichage titre, entreprise, lieu, salaire, description
  - Date de publication
  - Requirements, benefits
  - Bouton Postuler (lien externe)
  - Bouton Sauvegarder
  - Bouton Retour
- **Tests**:
  - Component test
  - Route test

### Tâche 3.4: Frontend - Optimisation UX
- **Description**: Améliorer l'expérience utilisateur
- **Type**: Frontend
- **Estim.**: 1h
- **Dépendances**: 
  - Tâche 3.3
- **Livrables**:
  - Scroll to top à l'arrivée
  - Loading state
  - Error handling
- **Tests**:
  - Component test

---

## Checklist Definition of Done

- [ ] Code review passé
- [ ] Tests unitaires >= 80% coverage
- [ ] Tests d'intégration passés
- [ ] E2E test passing
- [ ] Documentation mise à jour
