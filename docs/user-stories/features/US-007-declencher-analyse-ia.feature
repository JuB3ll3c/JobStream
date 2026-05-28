# Language: fr

Feature: Déclencher l'analyse IA d'une offre

  En tant que chercheur d'emploi
  Je veux lancer l'analyse IA d'une offre sauvegardée
  Afin d'obtenir une évaluation objective

  # ============================================================================
  # US-007: Analyse basique
  # ============================================================================

  Scenario: Lancer l'analyse d'une offre sauvegardée
    Given je suis sur la page "Offres Sauvegardées"
    And l'offre "Développeur Java" n'a pas encore été analysée
    When je clique sur le bouton "Analyser" de cette offre
    Then une demande d'analyse est envoyée
    And le bouton affiche "Analyse en cours..."
    And le statut "En cours" est affiché sur la carte

  Scenario: Lancer l'analyse depuis la page de détails
    Given je suis sur la page de détails d'une offre sauvegardée
    And cette offre n'a pas encore été analysée
    When je clique sur le bouton "Analyser"
    Then l'analyse est déclenchée
    And je suis notifié du début de l'analyse

  # ============================================================================
  # US-007: Analyse déjà en cours
  # ============================================================================

  Scenario: Tentative d'analyse alors qu'une analyse est déjà en cours
    Given l'offre "Dévelopreur Java" est en cours d'analyse
    When je clique sur le bouton "Analyser"
    Then un message "Analyse en cours" est affiché
    And aucune nouvelle analyse n'est déclenchée

  # ============================================================================
  # US-007: Analyse en erreur
  # ============================================================================

  Scenario: Relancer une analyse après un échec
    Given l'offre "Développeur Java" a échoué lors d'une analyse précédente
    When je clique sur le bouton "Réessayer" ou "Analyser"
    Then une nouvelle analyse est déclenchée

  # ============================================================================
  # US-007: Analyse de toutes les offres
  # ============================================================================

  Scenario: Analyser toutes les offres sauvegardées
    Given je suis sur la page "Offres Sauvegardées"
    And j'ai plusieurs offres non analysées
    When je clique sur le bouton "Analyser tout"
    Then l'analyse est déclenchée pour chaque offre non analysée
    And une barre de progression s'affiche
    And les statuts se mettent à jour en temps réel
