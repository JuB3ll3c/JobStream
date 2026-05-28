# Language: fr

Feature: Vérifier la santé de l'API

  En tant qu'utilisateur de l'application
  Je veux pouvoir vérifier que le service est opérationnel
  Afin de m'assurer que l'application fonctionne correctement

  # ============================================================================
  # US-010: Health check basique
  # ============================================================================

  Scenario: Vérifier que l'API est opérationnelle
    Given l'application est en cours d'exécution
    When je fais une requête GET sur /api/health
    Then la réponse est 200 OK
    And le status "UP" est retourné

  Scenario: API indisponible
    Given l'application rencontre un problème
    When je fais une requête GET sur /api/health
    Then le status "DOWN" est retourné

  # ============================================================================
  # US-010: Indicateur visuel
  # ============================================================================

  Scenario: Indicateur de santé dans le footer
    Given je suis sur n'importe quelle page de l'application
    Then un indicateur de santé est visible dans le footer
    And il est vert quand tout va bien
