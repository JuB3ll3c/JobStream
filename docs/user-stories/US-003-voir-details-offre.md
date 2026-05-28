# US-003 : Voir les détails d'une offre

## 📋 Détails
- **ID** : US-003
- **Titre** : Voir les détails d'une offre
- **Priorité** : 🔴 HIGH
- **Statut** : À faire
- **Sprint** : 1

## 👤 Persona
**Pierre**, 35 ans, développeur full-stack à Lausanne, consulte une offre qui l'intéresse.

## 🎯 Value
Permet au candidat de voir toutes les informations détaillées d'une offre d'emploi.

## ✅ Acceptance Criteria

### Critères obligatoires (Must Have)
- [ ] Afficher le titre du poste
- [ ] Afficher le nom de l'entreprise
- [ ] Afficher la localisation (ville, pays)
- [ ] Afficher le salaire (si disponible)
- [ ] Afficher la date de publication
- [ ] Afficher la description complète du poste
- [ ] Afficher un bouton "Postuler" qui ouvre le lien externe
- [ ] Afficher un bouton "Sauvegarder"

### Critères optionnels (Should Have)
- [ ] Afficher les exigences du poste
- [ ] Afficher les avantages/benefits
- [ ] Afficher le type de contrat (CDI, CDD, etc.)

### Critères UX
- [ ] Bouton "Retour" pour revenir à la liste
- [ ] Scroll fluide vers le haut à l'arrivée sur la page
- [ ] Loading state pendant le chargement des détails

## 🔧 Technical Notes

### API à appeler
```
GET /api/v1/jobs/{id}
```

### Response attendue
```json
{
  "id": "job-123",
  "title": "Développeur Full Stack",
  "company": "TechCorp",
  "location": "Lausanne, Switzerland",
  "salary": "CHF 80'000 - 120'000",
  "contractType": "CDI",
  "postedDate": "2026-03-20",
  "description": "...",
  "requirements": ["...", "..."],
  "benefits": ["...", "..."],
  "applyUrl": "https://..."
}
```

### Composants Angular
- `JobDetailComponent` - Page de détails
- `JobApplyButtonComponent` - Bouton postuler

## 🔗 Dependencies
- US-002 (Afficher les résultats) - dépend de cette US

## 🔄 User Flow
```
1. User clique sur une offre dans la liste (US-002)
2. Redirection vers page de détails
3. Affichage des informations complètes
4. User peut postuler ou sauvegarder
```