# Language: fr

Feature: Voir les détails d'une offre d'emploi

  En tant que chercheur d'emploi
  Je veux voir les détails complets d'une offre
  Afin de décider si je postule ou non

  # ============================================================================
  # US-003: Affichage des détails
  # ============================================================================

  Scenario: Affichage des informations principales
    Given je suis sur la page de détails d'une offre
    Then le titre du poste est affiché
    And le nom de l'entreprise est affiché
    And la localisation est affichée
    And le salaire est affiché si disponible

  Scenario: Affichage de la description complète
    Given je suis sur la page de détails d'une offre
    When je fais défiler la page
    Then la description complète du poste est visible
    And les exigences du poste sont affichées si disponibles

  Scenario: Affichage du type de contrat
    Given je suis sur la page de détails d'une offre
    Then le type de contrat est affiché (CDI, CDD, etc.)
    And la date de publication est affichée

  # ============================================================================
  # US-003: Actions disponibles
  # ============================================================================

  Scenario: Postuler à une offre via lien externe
    Given je suis sur la page de détails d'une offre
    When je clique sur le bouton "Postuler"
    Then un nouvel onglet vers l'URL originale de l'offre s'ouvre

  Scenario: Sauvegarder une offre depuis les détails
    Given je suis sur la page de détails d'une offre
    And l'offre n'est pas encore sauvegardée
    When je clique sur le bouton "Sauvegarder"
    Then l'offre est sauvegardée
    And le bouton devient "Sauvegardé"
    And un message de confirmation s'affiche

  Scenario: Retour à la liste des résultats
    Given je suis sur la page de détails d'une offre
    When je clique sur le bouton "Retour"
    Then je suis redirigé vers la page de recherche ou la liste précédente
