# Language: fr

Feature: Afficher les résultats de recherche

  En tant que chercheur d'emploi
  Je veux voir les résultats de recherche
  Afin de pouvoir evaluator les offres trouvées

  # ============================================================================
  # US-002: Affichage des résultats
  # ============================================================================

  Scenario: Affichage d'une liste d'offres
    Given la recherche a retourné des résultats
    When la page de résultats s'affiche
    Then chaque offre affiche le titre
    And chaque offre affiche le nom de l'entreprise
    And chaque offre affiche la localisation
    And chaque offre affiche le salaire si disponible

  Scenario: Navigation vers les détails d'une offre
    Given la liste des offres est affichée
    When je clique sur une carte d'offre
    Then je suis redirigé vers la page de détails de l'offre

  Scenario: Sauvegarder une offre depuis la liste
    Given la liste des offres est affichée
    When je clique sur le bouton "Sauvegarder" d'une offre
    Then l'offre est sauvegardée
    And un message de confirmation s'affiche

  # ============================================================================
  # US-002: Pagination
  # ============================================================================

  Scenario: Navigation vers la page suivante
    Given je suis sur la page 1 des résultats
    When je clique sur le bouton "Page suivante"
    Then la page 2 des résultats s'affiche
    And l'URL contient le paramètre page=2

  Scenario: Retour à la page précédente
    Given je suis sur la page 2 des résultats
    When je clique sur le bouton "Page précédente"
    Then la page 1 des résultats s'affiche

  # ============================================================================
  # US-002: États vides
  # ============================================================================

  Scenario: Aucun résultat de recherche
    Given la recherche a retourné zero résultat
    When la page de résultats s'affiche
    Then le message "Aucune offre trouvée" est affiché
    And un bouton "Nouvelle recherche" est disponible
