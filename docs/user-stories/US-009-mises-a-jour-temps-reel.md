# US-009 : Recevoir les mises à jour en temps réel (SSE)

## 📋 Détails
- **ID** : US-009
- **Titre** : Recevoir les mises à jour en temps réel (SSE)
- **Priorité** : 🟡 MEDIUM
- **Statut** : À faire
- **Sprint** : 2

## 👤 Persona
**Pierre** veut être informé immédiatement quand l'analyse IA de ses offres est terminée.

## 🎯 Value
Améliore l'expérience utilisateur en montrant les mises à jour en temps réel sans recharger la page.

## ✅ Acceptance Criteria

### Critères obligatoires (Must Have)
- [ ] Connexion SSE établie à la page "Offres Sauvegardées"
- [ ] Notification quand une analyse est terminée
- [ ] Mise à jour automatique du statut sur les cartes
- [ ] Indicateur de connexion SSE visible
- [ ] Reconnexion automatique en cas de déconnexion

### Critères optionnels (Should Have)
- [ ] Notification sonore à la fin d'une analyse
- [ ] Badge sur l'onglet du navigateur (nombre d'analyses terminées)

## 🔧 Technical Notes

### Endpoint SSE
```
GET /api/v1/events
```

### Eventstypes
```json
{
  "type": "ANALYSIS_COMPLETED",
  "payload": {
    "savedJobId": "saved-456",
    "analysisId": "analysis-789"
  }
}
```

```json
{
  "type": "ANALYSIS_FAILED",
  "payload": {
    "savedJobId": "saved-456",
    "error": "..."
  }
}
```

### Implémentation Angular
- Service `SseService` pour gérer la connexion
- Utiliser `EventSource` du navigateur
- Gestion du retry automatique

### Composants Angular
- `SseIndicatorComponent` - Indicateur de connexion
- `ToastNotificationComponent` - Notifications

## 🔗 Dependencies
- US-007 (Déclencher analyse IA) - génère les événements

## 🔄 User Flow
```
1. User arrive sur page "Offres Sauvegardées"
2. Connexion SSE établie (indicateur vert)
3. User déclenche analyse (US-007)
4. Analyse terminée → Notification + mise à jour carte
```