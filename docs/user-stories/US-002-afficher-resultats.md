# US-002 : Afficher les résultats de recherche

## 📋 Détails
- **ID** : US-002
- **Titre** : Afficher les résultats de recherche
- **Priorité** : 🔴 HIGH
- **Statut** : À faire
- **Sprint** : 1

## 👤 Persona
**Pierre**, 35 ans, développeur full-stack à Lausanne, cherche un nouveau poste.

## 🎯 Value
Permet au candidat de parcourir et évaluer la liste des offres correspondant à sa recherche.

## ✅ Acceptance Criteria

### Critères obligatoires (Must Have)
- [ ] Afficher la liste des offres sous forme de cartes
- [ ] Chaque carte contient : titre du poste, entreprise, localisation, salaire
- [ ] Support de la pagination (20 offres par page)
- [ ] Afficher le nombre total de résultats ("X offres trouvées")
- [ ] Click sur une carte renvoie vers US-003 (page de détails)
- [ ] Bouton "Sauvegarder" sur chaque carte

### Critères optionnels (Should Have)
- [ ] Afficher un skeleton loading pendant le chargement
- [ ] Afficher un message "Aucune offre trouvée" si结果是空的

### Critères UX
- [ ] Effet de survol subtil sur les cartes (ombre légère)
- [ ] Layout responsive adapté au desktop

## 🔧 Technical Notes

### API à appeler
```
GET /api/v1/jobs/search?query={keyword}&page={page}&limit={limit}
```

### Response attendue
```json
{
  "jobs": [
    {
      "id": "job-123",
      "title": "Développeur Full Stack",
      "company": "TechCorp",
      "location": "Lausanne, Switzerland",
      "salary": "CHF 80'000 - 120'000",
      "postedDate": "2026-03-20"
    }
  ],
  "total": 150,
  "page": 1,
  "limit": 20
}
```

### Composants Angular
- `JobListComponent` - Liste des offres
- `JobCardComponent` - Carte d'offre
- `PaginationComponent` - Pagination
- `SkeletonLoaderComponent` - Loader

## 🔗 Dependencies
- US-001 (Rechercher des offres) - doit être fait avant

## 🔄 User Flow
```
1. L'utilisateur lance la recherche (US-001)
2. Affichage de la liste des offres
3. L'utilisateur peut sauvegarder ou voir les détails
```