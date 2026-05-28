# Language: fr

Feature: Voir le résultat de l'analyse IA

  En tant que chercheur d'emploi
  Je veux voir les détails de l'analyse IA
  Afin de prendre une décision éclairée sur une offre

  # ============================================================================
  # US-008: Affichage du résultat
  # ============================================================================

  Scenario: Affichage du résumé de l'analyse
    Given l'offre "Développeur Java" a été analysée
    When j'accède aux détails de l'analyse
    Then le résumé généré par l'IA est affiché
    And le score de correspondance (0-100) est affiché

  Scenario: Affichage des points forts
    Given l'analyse de l'offre est terminée
    When je consulte les résultats de l'analyse
    Then les points forts identifiés sont affichés dans une liste

  Scenario: Affichage des points faibles
    Given l'analyse de l'offre est terminée
    When je consulte les résultats de l'analyse
    Then les points faibles identifiés sont affichés dans une liste

  # ============================================================================
  # US-008: Informations supplémentaires
  # ============================================================================

  Scenario: Affichage des recommandations
    Given l'analyse de l'offre est terminée
    When je consulte les résultats de l'analyse
    Then les recommandations personnalisées sont affichées

  Scenario: Affichage des tags
    Given l'analyse de l'offre est terminée
    When je consulte les résultats de l'analyse
    Then les tags (technologies, remote, etc.) sont affichés

  Scenario: Affichage du statut de l'analyse
    Given j'ai lancé une analyse pour une offre
    When je consulte la page de l'offre
    Then le statut de l'analyse est affiché (Terminé, En cours, Échoué)

  # ============================================================================
  # US-008: Analyse en cours
  # ============================================================================

  Scenario: Consultation pendant l'analyse en cours
    Given l'analyse de l'offre est en cours
    When je consulte la page des détails de l'offre
    Then le message "Analyse en cours..." est affiché
    And une barre de progression ou un indicateur de chargement est affiché
    And les résultats ne sont pas encore visibles
