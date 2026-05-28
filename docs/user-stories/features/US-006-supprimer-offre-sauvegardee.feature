# Language: fr

Feature: Supprimer une offre sauvegardée

  En tant que chercheur d'emploi
  Je veux supprimer une offre de ma liste
  Afin de gérer mes offres sauvegardées

  # ============================================================================
  # US-006: Suppression basique
  # ============================================================================

  Scenario: Supprimer une offre avec confirmation
    Given je suis sur la page "Offres Sauvegardées"
    And l'offre "Développeur Java" est dans ma liste
    When je clique sur le bouton "Supprimer" de cette offre
    Then une fenêtre de confirmation s'affiche
    And le message "Voulez-vous vraiment supprimer cette offre ?" est affiché
    When je clique sur "Confirmer"
    Then l'offre est supprimée de la base de données
    And l'offre disparaît de la liste
    And le message "Offre supprimée" s'affiche

  # ============================================================================
  # US-006: Annulation
  # ============================================================================

  Scenario: Annuler la suppression
    Given je suis sur la page "Offres Sauvegardées"
    And l'offre "Développeur Java" est dans ma liste
    When je clique sur le bouton "Supprimer" de cette offre
    And une fenêtre de confirmation s'affiche
    When je clique sur "Annuler"
    Then la fenêtre se ferme
    And l'offre reste dans la liste

  # ============================================================================
  # US-006: Erreurs
  # ============================================================================

  Scenario: Erreur lors de la suppression
    Given je suis sur la page "Offres Sauvegardées"
    And le service de suppression est indisponible
    When je clique sur le bouton "Supprimer" d'une offre
    And je confirme la suppression
    Then un message d'erreur "Impossible de supprimer l'offre" est affiché
    And l'offre reste dans la liste
