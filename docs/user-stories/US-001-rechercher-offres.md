# US-001: Rechercher des offres par mot-clé

## 📋 Détails
- **ID**: US-001
- **Titre**: Rechercher des offres par mot-clé
- **Priorité**: 🔴 HIGH
- **Statut**: À faire
- **Sprint**: 1

## 👤 Persona
**Pierre**, 35 ans, développeur full-stack à Lausanne, cherche un nouveau poste.

## 🎯 Value
Permet aux chercheurs d'emploi de trouver rapidement des offres correspondant à leurs critères de recherche.

## ✅ Acceptance Criteria

### Critères obligatoires (Must Have)
- [ ] L'utilisateur peut saisir un mot-clé dans un champ de recherche
- [ ] L'utilisateur peut lancer la recherche via un bouton ou entrée
- [ ] La recherche retourne des résultats en moins de 3 secondes
- [ ] Si aucun résultat, un message "Aucune offre trouvée" s'affiche
- [ ] Si erreur API, un message d'erreur approprié s'affiche

### Critères optionnels (Should Have)
- [ ] La recherche est debounce-ée (300ms) pour éviter trop d'appels
- [ ] Un indicateur de chargement s'affiche pendant la recherche

### Critères UX
- [ ] Le champ de recherche est focus à l'arrivée sur la page
- [ ] Le placeholder indique "Développeur, Chef de projet, ..."

## 🔧 Technical Notes

### API à appeler
```
GET /api/v1/jobs/search?query={keyword}&page={page}&limit={limit}
```

### Response attendue
```json
{
  "jobs": [...],
  "total": 150,
  "page": 1,
  "limit": 20
}
```

### Composants Angular
- `SearchComponent` - champ de recherche + bouton
- `JobSearchService` - service pour l'API search

## 🔗 Dependencies
- US-002 (Afficher les résultats) - dépend de cette US

## 🔄 User Flow
```
1. Arrivée sur page recherche
2. Focus sur champ de recherche
3. Saisie mot-clé
4. Appui Entrée ou clic Recherche
5. → US-002: Affichage résultats
```