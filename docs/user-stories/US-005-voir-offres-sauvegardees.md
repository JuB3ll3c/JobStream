# US-005 : Voir mes offres sauvegardées

## 📋 Détails
- **ID** : US-005
- **Titre** : Voir mes offres sauvegardées
- **Priorité** : 🔴 HIGH
- **Statut** : À faire
- **Sprint** : 1

## 👤 Persona
**Pierre** veut revoir les offres qu'il a sauvegardées pour faire son choix.

## 🎯 Value
Permet au candidat d'accéder rapidement à sa collection d'offres intéressantes.

## ✅ Acceptance Criteria

### Critères obligatoires (Must Have)
- [ ] Accès via navigation "Offres Sauvegardées"
- [ ] Afficher la liste des offres sauvegardées
- [ ] Chaque carte affiche : titre, entreprise, lieu, date de sauvegarde
- [ ] Bouton "Analyser" sur chaque offre
- [ ] Bouton "Supprimer" sur chaque offre
- [ ] Nombre total d'offres affichées ("X offres sauvegardées")

### Critères optionnels (Should Have)
- [ ] Tri par date de sauvegarde (plus récent)
- [ ] Tri par titre d'offre
- [ ] Empty state si aucune offre sauvegardée

## 🔧 Technical Notes

### API à appeler
```
GET /api/v1/saved-jobs
```

### Response
```json
{
  "savedJobs": [
    {
      "id": "saved-456",
      "jobId": "job-123",
      "title": "Développeur Full Stack",
      "company": "TechCorp",
      "location": "Lausanne",
      "salary": "CHF 80'000 - 120'000",
      "savedAt": "2026-03-26T10:30:00Z"
    }
  ],
  "total": 5
}
```

### Composants Angular
- `SavedJobsComponent` - Page principale
- `SavedJobCardComponent` - Carte d'offre sauvegardée

## 🔗 Dependencies
- US-004 (Sauvegarder une offre) - doit être fait avant

## 🔄 User Flow
```
1. User clique "Offres Sauvegardées" dans la nav
2. Affichage liste des offres sauvegardées
3. User peut analyser, supprimer, ou cliquer pour voir détails
```