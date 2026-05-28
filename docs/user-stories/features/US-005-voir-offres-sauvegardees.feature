# Language: fr

Feature: Voir mes offres sauvegardées

  En tant que chercheur d'emploi
  Je veux voir la liste de mes offres sauvegardées
  Afin de les comparer et choisir celle qui m'intéresse le plus

  # ============================================================================
  # US-005: Affichage de la liste
  # ============================================================================

  Scenario: Affichage de la liste des offres sauvegardées
    Given j'ai des offres sauvegardées dans ma liste
    When j'accède à la page "Offres Sauvegardées"
    Then la liste de toutes mes offres sauvegardées s'affiche
    And chaque carte affiche le titre
    And chaque carte affiche l'entreprise
    And chaque carte affiche la localisation
    And chaque carte affiche la date de sauvegarde

  Scenario: Affichage du nombre d'offres sauvegardées
    Given j'ai 5 offres sauvegardées
    When j'accède à la page "Offres Sauvegardées"
    Then le message "5 offres sauvegardées" est affiché

  Scenario: Accès via la navigation
    Given je suis sur n'importe quelle page de l'application
    When je clique sur "Offres Sauvegardées" dans le menu
    Then je suis redirigé vers la page des offres sauvegardées

  # ============================================================================
  # US-005: Actions sur les offres
  # ============================================================================

  Scenario: Analyser une offre sauvegardée
    Given je suis sur la page des offres sauvegardées
    When je clique sur le bouton "Analyser" d'une offre
    Then l'analyse IA est déclenchée
    And le statut "Analyse en cours" est affiché

  Scenario: Supprimer une offre sauvegardée
    Given je suis sur la page des offres sauvegardées
    When je clique sur le bouton "Supprimer" d'une offre
    Then une confirmation est demandée
    When je confirme la suppression
    Then l'offre est retirée de la liste
    And le message "Offre supprimée" s'affiche

  # ============================================================================
  # US-005: États vides
  # ============================================================================

  Scenario: Aucune offre sauvegardée
    Given je n'ai aucune offre sauvegardée
    When j'accède à la page "Offres Sauvegardées"
    Then le message "Aucune offre sauvegardée" est affiché
    And une illustration est affichée
    And un bouton "Rechercher des offres" est disponible
