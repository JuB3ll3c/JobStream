# US-004 : Sauvegarder une offre

## 📋 Détails
- **ID** : US-004
- **Titre** : Sauvegarder une offre
- **Priorité** : 🔴 HIGH
- **Statut** : À faire
- **Sprint** : 1

## 👤 Persona
**Pierre** a trouvé une offre intéressante et veut la sauvegarder pour la consulter plus tard.

## 🎯 Value
Permet au candidat de conserver des offres qui l'intéressent pour comparaison ultérieure.

## ✅ Acceptance Criteria

### Critères obligatoires (Must Have)
- [ ] Bouton "Sauvegarder" sur chaque offre dans la liste (US-002)
- [ ] Bouton "Sauvegarder" sur la page de détails (US-003)
- [ ] Sauvegarde en base de données via API POST
- [ ] Confirmation visuelle (toast/notification) après sauvegarde
- [ ] Le bouton se transforme en "Sauvegardé" après action
- [ ] Message d'erreur si la sauvegarde échoue

### Critères optionnels (Should Have)
- [ ] Ne pas autoriser les doublons (même offre déjà sauvegardée)
- [ ] Message "Déjà sauvegardée" si tentative de double sauvegarde

## 🔧 Technical Notes

### API à appeler
```
POST /api/v1/saved-jobs
```

### Request body
```json
{
  "jobId": "job-123",
  "title": "Développeur Full Stack",
  "company": "TechCorp",
  "location": "Lausanne, Switzerland",
  "salary": "CHF 80'000 - 120'000",
  "postedDate": "2026-03-20",
  "applyUrl": "https://..."
}
```

### Response
```json
{
  "id": "saved-456",
  "jobId": "job-123",
  "savedAt": "2026-03-26T10:30:00Z"
}
```

### Composants Angular
- `SaveJobButtonComponent` - Bouton de sauvegarde
- `SavedJobsService` - Service pour API

## 🔗 Dependencies
- US-002 (Afficher les résultats) - affichage du bouton
- US-003 (Voir détails) - affichage du bouton

## 🔄 User Flow
```
1. User clique "Sauvegarder" sur une offre
2. Appel API POST /saved-jobs
3. Succès → Toast "Offre sauvegardée", bouton devient "Sauvegardé"
4. Échec → Toast erreur "Impossible de sauvegarder"
```