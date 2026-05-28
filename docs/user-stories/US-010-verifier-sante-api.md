# US-010 : Vérifier la santé de l'API

## 📋 Détails
- **ID** : US-010
- **Titre** : Vérifier la santé de l'API
- **Priorité** : 🟢 LOW
- **Statut** : À faire
- **Sprint** : 1

## 👤 Persona
**Pierre** veut s'assurer que l'application fonctionne correctement.

## 🎯 Value
Permet de s'assurer que tous les services sont opérationnels.

## ✅ Acceptance Criteria

### Critères obligatoires (Must Have)
- [ ] Endpoint `/api/v1/health` accessible
- [ ] Retourne le statut de tous les services (API, Kafka, DB)

### Critères optionnels (Should Have)
- [ ] Indicateur de santé visible dans le footer ou header

## 🔧 Technical Notes

### API à appeler
```
GET /api/v1/health
```

### Response
```json
{
  "status": "UP",
  "components": {
    "api": "UP",
    "kafka": "UP",
    "database": "UP"
  }
}
```

### Response (si problème)
```json
{
  "status": "DOWN",
  "components": {
    "api": "UP",
    "kafka": "DOWN",
    "database": "UP"
  }
}
```

### Composants Angular
- `HealthIndicatorComponent` - Indicateur visuel

## 🔗 Dependencies
- Aucune dépendance

## 🔄 User Flow
```
1. User arrive sur l'application
2. Vérification automatique de la santé
3. Si problème → Message d'avertissement
4. Si OK → Tout fonctionne normalement
```