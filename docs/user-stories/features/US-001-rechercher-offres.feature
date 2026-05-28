# Language: fr

Feature: Rechercher des offres d'emploi par mot-clé

  En tant que chercheur d'emploi
  Je veux rechercher des offres par mot-clé
  Afin de trouver rapidement des postes correspondant à mes critères

  # ============================================================================
  # US-001: Scénarios de recherche basique
  # ============================================================================

  Scenario: Recherche avec un mot-clé valide
    Given je suis sur la page de recherche
    When je saisis "développeur java" dans le champ de recherche
    And je clique sur le bouton "Rechercher"
    Then une liste d'offres s'affiche
    And le nombre total de résultats est affiché

  Scenario: Recherche avec un mot-clé sans résultat
    Given je suis sur la page de recherche
    When je saisis "xyzabc123invalide" dans le champ de recherche
    And je clique sur le bouton "Rechercher"
    Then le message "Aucune offre trouvée" est affiché

  Scenario: Recherche avec un mot-clé et paramètres de pagination
    Given je suis sur la page de recherche
    When je saisis "développeur" dans le champ de recherche
    And je clique sur le bouton "Rechercher"
    Then la première page d'offres s'affiche
    And les boutons de pagination sont disponibles

  # ============================================================================
  # US-001: Scénarios d'erreurs
  # ============================================================================

  Scenario: Recherche sans mot-clé
    Given je suis sur la page de recherche
    When je clique sur le bouton "Rechercher"
    Then un message d'erreur "Veuillez saisir un mot-clé" est affiché
    And le champ de recherche est mis en évidence

  Scenario: Erreur lors de l'appel à l'API externe
    Given je suis sur la page de recherche
    When je saisis "développeur" dans le champ de recherche
    And le service externe est indisponible
    And je clique sur le bouton "Rechercher"
    Then un message d'erreur "Une erreur est survenue lors de la recherche" est affiché
