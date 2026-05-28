# US-001: Rechercher des offres par mot-clé

## Objectif
Permettre aux chercheurs d'emploi de trouver rapidement des offres correspondant à leurs critères.

---

## Tâches

### Tâche 1.1: Backend - Controller de recherche
- **Description**: Créer l'endpoint GET /api/jobs
- **Type**: Backend
- **Estim.**: 1h
- **Dépendances**: 
  - openapi.yml (existant)
- **Livrables**:
  - `JobSearchController.java` avec endpoint `/api/jobs`
  - DTOs générés depuis OpenAPI
  - Mapping des params query (q, page, limit)
- **Tests**:
  - Unit test controller
  - Test avec mock JSearch API

### Tâche 1.2: Backend - Service JSearch
- **Description**: Intégrer l'appel à l'API JSearch externe
- **Type**: Backend
- **Estim.**: 2h
- **Dépendances**: 
  - Tâche 1.1
- **Livrables**:
  - `JSearchService.java` - appel API externe
  - Configuration (API key, base URL)
  - Gestion des erreurs (timeout, rate limit)
- **Tests**:
  - Unit test service
  - Test d'intégration (ou mock)

### Tâche 1.3: Frontend - Service de recherche
- **Description**: Créer le service Angular pour appeler l'API
- **Type**: Frontend
- **Estim.**: 1h
- **Dépendances**: 
  - Tâche 1.1 (API définie)
- **Livrables**:
  - `JobSearchService` - méthode `search(query, page, limit)`
  - Types générés depuis OpenAPI
  - Gestion des erreurs
- **Tests**:
  - Unit test service avec HttpTestingModule

### Tâche 1.4: Frontend - Composant Search
- **Description**: Créer le champ de recherche et le bouton
- **Type**: Frontend
- **Estim.**: 2h
- **Dépendances**: 
  - Tâche 1.3
- **Livrables**:
  - `SearchComponent` - input + bouton
  - Debounce 300ms
  - Gestion du state (loading, error, results)
  - Routing vers page résultats
- **Tests**:
  - Component test
  - E2E test recherche

### Tâche 1.5: Frontend - Page结果的显示
- **Description**: Afficher les résultats de recherche (intégré US-002)
- **Type**: Frontend
- **Estim.**: 3h
- **Dépendances**: 
  - Tâche 1.3, 1.4
- **Livrables**:
  - `JobListComponent` - liste des offres
  - `JobCardComponent` - carte individuelle
  - `PaginationComponent` - pagination
  - Empty state
  - Skeleton loading
- **Tests**:
  - Component tests
  - E2E test affichage

---

## Checklist Definition of Done

- [ ] Code review passé
- [ ] Tests unitaires >= 80% coverage
- [ ] Tests d'intégration passés
- [ ] E2E test passing
- [ ] Documentation mise à jour
