# US-008 : Voir le résultat de l'analyse IA

## 📋 Détails
- **ID** : US-008
- **Titre** : Voir le résultat de l'analyse IA
- **Priorité** : 🟡 MEDIUM
- **Statut** : À faire
- **Sprint** : 2

## 👤 Persona
**Pierre** veut voir le résumé et l'évaluation de l'IA pour une offre analysée.

## 🎯 Value
Permet au candidat de prendre une décision éclairée grâce à l'analyse IA.

## ✅ Acceptance Criteria

### Critères obligatoires (Must Have)
- [ ] Afficher le résumé généré par l'IA (2-3 phrases)
- [ ] Afficher le score de correspondance (0-100%)
- [ ] Afficher les points forts (liste)
- [ ] Afficher les points faibles (liste)
- [ ] Afficher le statut de l'analyse (Terminé / En cours / Échoué)

### Critères optionnels (Should Have)
- [ ] Afficher les recommandations personnalisées

## 🔧 Technical Notes

### API à appeler
```
GET /api/v1/analyses/{id}
```

### Response
```json
{
  "id": "analysis-789",
  "savedJobId": "saved-456",
  "status": "COMPLETED",
  "summary": "Excellente opportunité pour un développeur full-stack...",
  "score": 85,
  "strengths": [
    "Stack technique moderne",
    "Salaire compétitif",
    "Remote possible"
  ],
  "weaknesses": [
    "Horaire potentiellement intensif",
    "Entreprise récente"
  ],
  "recommendations": [
    "Préparer des exemples de projets frontend"
  ],
  "completedAt": "2026-03-26T10:32:00Z"
}
```

### Flux SSE
- Le frontend s'abonne aux événements pour cette analyse
- Reçoit les mises à jour en temps réel

### Composants Angular
- `AnalysisResultComponent` - Affichage des résultats
- `AnalysisCardComponent` - Carte résumée sur la liste

## 🔗 Dependencies
- US-007 (Déclencher analyse IA) - doit être fait avant

## 🔄 User Flow
```
1. Analyse terminée (US-007)
2. User voit "Analyse terminée" sur la carte
3. User clique pour voir les détails
4. Affichage résumé, score, forces, faiblesses
```