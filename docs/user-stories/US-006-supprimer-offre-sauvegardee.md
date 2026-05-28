# US-006 : Supprimer une offre sauvegardée

## 📋 Détails
- **ID** : US-006
- **Titre** : Supprimer une offre sauvegardée
- **Priorité** : 🔴 HIGH
- **Statut** : À faire
- **Sprint** : 1

## 👤 Persona
**Pierre** a décider de ne plus postuler à une offre et veut la supprimer de sa liste.

## 🎯 Value
Permet au candidat de gérer sa liste d'offres sauvegardées en supprimant celles qui ne l'intéressent plus.

## ✅ Acceptance Criteria

### Critères obligatoires (Must Have)
- [ ] Bouton "Supprimer" sur chaque offre de la page "Offres Sauvegardées"
- [ ] Confirmation avant suppression (modal ou confirm dialog)
- [ ] Suppression via API DELETE
- [ ] Retirer l'offre de la liste après suppression
- [ ] Confirmation visuelle (toast) après suppression

### Critères optionnels (Should Have)
- [ ] Bouton undo pendant 5 secondes après suppression

## 🔧 Technical Notes

### API à appeler
```
DELETE /api/v1/saved-jobs/{id}
```

### Response
```
204 No Content
```

### Composants Angular
- `SavedJobsComponent` - Gestion du bouton supprimer
- `ConfirmDialogComponent` - Dialogue de confirmation

## 🔗 Dependencies
- US-005 (Voir offres sauvegardées) - affichage du bouton

## 🔄 User Flow
```
1. User clique "Supprimer" sur une offre
2. Dialogue de confirmation s'affiche
3. User confirme → API DELETE
4. Succès → Toast "Offre supprimée", offre retirée de la liste
```