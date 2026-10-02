# Roadmap des pages métier

## Objectif

Cette roadmap organise la livraison des trois parcours métier suivants :

- rechercher des offres d'emploi externes ;
- consulter le détail d'une offre ;
- gérer les offres sauvegardées par l'utilisateur connecté.

Le périmètre authentification, rôles, navbar et style initial est considéré comme terminé. Chaque tranche de cette roadmap devra être explicitement approuvée avant son implémentation, conformément au workflow du dépôt.

## Décisions validées

### Recherche

- La recherche accepte un poste ou des mots-clés, une localité et des paramètres de pagination.
- La pagination publique commence à `1`.
- `page` vaut `1` par défaut et ne peut pas être inférieur à `1`.
- `size` vaut `20` par défaut.
- Les critères de recherche et la page courante sont conservés dans l'URL du navigateur.
- Le champ de recherche est présenté comme « Poste ou mots-clés » : Adzuna effectue une recherche approximative et ne garantit pas une correspondance exacte avec un intitulé.
- Aucun post-filtrage local ne doit éliminer les résultats retournés par Adzuna, car cela rendrait les pages et les totaux incohérents.

### Indépendance vis-à-vis d'Adzuna

Le front consomme une interface JobStream stable et ne connaît ni le nom du fournisseur ni ses paramètres. L'adapter Adzuna traduit les critères JobStream vers le fournisseur externe.

Interface de recherche envisagée :

```http
GET /job-offers?title=Java%20Developer&location=Zurich&page=1&size=20
```

Réponse paginée normalisée :

```text
content
page
size
totalElements
totalPages
```

Correspondance initiale avec Adzuna :

| JobStream | Adzuna |
|---|---|
| `title` | `what` |
| `location` | `where` |
| `page` | numéro de page dans le chemin de recherche |
| `size` | `results_per_page` |

L'interface exacte reste à formaliser dans `openapi/openapi.yaml` pendant la première tranche. La documentation officielle d'Adzuna décrit les capacités de recherche par mots-clés, localisation et pagination : <https://developer.adzuna.com/docs/search>.

### Offres sauvegardées

- Une offre sauvegardée appartient à l'utilisateur authentifié.
- L'utilisateur est déduit du contexte d'authentification ; il n'est jamais fourni par le front dans la requête.
- La liste, le détail et la suppression sont limités aux offres de l'utilisateur connecté.
- Un doublon est défini par le couple `(user_id, external_id)`.
- Une offre appartenant à un autre utilisateur n'est pas révélée et est traitée comme introuvable.
- L'implémentation de cette décision nécessitera une approbation explicite du changement de schéma.

### Détail et sauvegarde

- Une présentation commune affiche les informations d'une offre externe ou sauvegardée.
- L'offre expose son titre, son entreprise, sa localité, sa date, son type de contrat, son salaire, sa description disponible, ses exigences et son lien original.
- Adzuna ne fournissant qu'un extrait de description dans ses résultats publics, JobStream ne promet pas une description intégrale.
- La sauvegarde est proposée depuis chaque résultat de recherche ainsi que depuis la page de détail.
- Un conflit de sauvegarde est présenté comme « déjà sauvegardée », et non comme une erreur technique générique.

## Ordre des tranches

### 1. Contrat de recherche JobStream

Résultat attendu : le contrat OpenAPI décrit une recherche indépendante du fournisseur avec `title`, `location`, `page` et `size`, ainsi qu'une réponse paginée normalisée.

À couvrir :

- valeurs par défaut et validation des paramètres ;
- numérotation des pages à partir de `1` ;
- réponse vide ;
- erreur de validation ;
- indisponibilité du fournisseur externe ;
- génération des interfaces backend et du client Angular depuis OpenAPI.

### 2. Adapter de recherche Adzuna

Résultat attendu : le backend traduit les critères JobStream vers Adzuna et retourne le modèle paginé normalisé.

À couvrir :

- traduction de `title`, `location`, `page` et `size` ;
- normalisation des résultats et des métadonnées de pagination ;
- absence de fuite des paramètres ou erreurs propres à Adzuna ;
- tests comportementaux sans appel réel au fournisseur.

### 3. Propriété des offres sauvegardées

Résultat attendu : toutes les opérations sur les offres sauvegardées sont restreintes à l'utilisateur authentifié.

À couvrir :

- rattachement lors de la sauvegarde ;
- unicité par utilisateur et offre externe ;
- liste paginée personnelle ;
- consultation et suppression par le propriétaire uniquement ;
- comportement lorsqu'une offre est absente ou appartient à un autre utilisateur ;
- adaptation du schéma et des données de test après approbation.

### 4. Page de recherche

Résultat attendu : la route `/jobs` permet de rechercher des offres par poste ou mots-clés et par localité.

À couvrir :

- formulaire et validation ;
- synchronisation des critères avec les paramètres de l'URL ;
- chargement automatique d'une recherche présente dans l'URL ;
- liste de résultats ;
- pagination commençant à `1` ;
- états initial, chargement, vide, erreur de validation, erreur fournisseur et session expirée ;
- accès au détail depuis chaque résultat ;
- bouton de sauvegarde sur chaque offre ;
- états disponible, en cours, sauvegardée et déjà sauvegardée au niveau de l'offre concernée ;
- prévention des doubles soumissions sans bloquer les autres résultats.

### 5. Page de détail d'une offre externe

Résultat attendu : une route dédiée présente les informations disponibles pour une offre sélectionnée.

À couvrir :

- chargement et affichage du détail normalisé ;
- lien vers l'annonce originale ;
- retour vers la recherche en conservant ses critères et sa page ;
- états de chargement, offre introuvable et erreur fournisseur ;
- stratégie de récupération compatible avec les limites d'Adzuna, sans dépendre de l'implémentation backend actuelle.

### 6. Sauvegarde depuis la recherche et le détail

Résultat attendu : l'utilisateur peut sauvegarder une offre depuis la liste des résultats ou depuis sa page de détail.

À couvrir :

- comportement de sauvegarde partagé entre les deux pages ;
- états disponible, en cours, sauvegardée et déjà sauvegardée ;
- prévention des doubles soumissions ;
- erreur fonctionnelle distincte d'une erreur technique ;
- cohérence avec l'unicité par utilisateur.

### 7. Page des offres sauvegardées

Résultat attendu : une route `/saved-jobs` affiche la collection personnelle de l'utilisateur.

À couvrir :

- liste paginée ;
- tri initial par date de sauvegarde décroissante ;
- états chargement, collection vide et erreur ;
- conservation de la page et du tri dans l'URL ;
- accès au détail d'une offre sauvegardée.

### 8. Détail et suppression d'une offre sauvegardée

Résultat attendu : une route `/saved-jobs/:id` réutilise la présentation du détail et permet de retirer l'offre de la collection.

À couvrir :

- consultation par le propriétaire ;
- confirmation avant suppression ;
- retour cohérent vers la liste ;
- pagination correcte après suppression du dernier élément d'une page ;
- offre absente ou non accessible.

### 9. Parcours transversal

Résultat attendu : les trois pages forment un parcours cohérent et utilisable sur les tailles d'écran prises en charge.

À couvrir :

- liens vers la recherche et les offres sauvegardées dans la navigation existante ;
- protection des routes par l'authentification existante ;
- navigation clavier, libellés accessibles et annonces des erreurs ;
- responsive ;
- cohérence visuelle avec les styles globaux existants ;
- scénarios d'acceptation couvrant recherche, détail, sauvegarde et suppression.

## Dépendances entre les tranches

```text
Contrat de recherche
        |
        v
Adapter Adzuna ---> Page de recherche ---> Détail externe ---> Sauvegarde
                                                               |
Propriété des sauvegardes --------------------------------------+
        |
        v
Liste sauvegardée ---> Détail et suppression ---> Parcours transversal
```

## Hors périmètre

- analyse de compatibilité entre une offre et un CV ;
- génération de lettres de motivation ;
- Kafka, traitement asynchrone et mises à jour temps réel ;
- recherche multi-fournisseurs ;
- filtres avancés tels que salaire, contrat, télétravail ou ancienneté de l'annonce.

Ces sujets pourront être ajoutés après validation du parcours métier de base.
