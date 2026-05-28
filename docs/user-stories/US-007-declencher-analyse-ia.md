# US-007 : Déclencher l'analyse IA d'une offre

## 📋 Détails
- **ID** : US-007
- **Titre** : Déclencher l'analyse IA d'une offre
- **Priorité** : 🟡 MEDIUM
- **Statut** : À faire
- **Sprint** : 2

## 👤 Persona
**Pierre** veut obtenir une analyse IA de ses offres sauvegardées pour mieux les évaluer.

## 🎯 Value
Permet au candidat d'avoir une analyse objective et détaillée de chaque offre via l'IA.

## ✅ Acceptance Criteria

### Critères obligatoires (Must Have)
- [ ] Bouton "Analyser" sur chaque offre sauvegardée (US-005)
- [ ] Bouton "Analyser" sur la page de détails d'une offre sauvegardée
- [ ] Déclenchement de l'analyse via Kafka message
- [ ] Indicateur de statut pendant l'analyse (En cours...)
- [ ] Mise à jour automatique du statut via SSE

### Critères optionnels (Should Have)
- [ ] Bouton "Analyser tout" pour lancer l'analyse de toutes les offres

## 🔧 Technical Notes

### API à appeler
```
POST /api/v1/analyses
```

### Request body
```json
{
  "savedJobId": "saved-456"
}
```

### Response
```json
{
  "id": "analysis-789",
  "savedJobId": "saved-456",
  "status": "PENDING",
  "createdAt": "2026-03-26T10:30:00Z"
}
```

### Flux Kafka
1. API POST /analyses → Publishing vers topic `job-offers-raw`
2. IA Consumer traite le message → Publish vers `job-offers-analyzed`
3. Dashboard met à jour la DB
4. SSE notifie le frontend

### Composants Angular
- `AnalyzeButtonComponent` - Bouton d'analyse
- `AnalysisStatusComponent` - Indicateur de statut

## 🔗 Dependencies
- US-005 (Voir offres sauvegardées) - affichage du bouton

## 🔄 User Flow
```
1. User clique "Analyser" sur une offre
2. Bouton devient "Analyse en cours..."
3. SSE reçoit mise à jour du statut
4. Analyse terminée → Statut "Terminé", accès aux résultats (US-008)
```