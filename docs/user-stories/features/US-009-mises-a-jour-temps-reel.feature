# Language: fr

Feature: Recevoir les mises à jour en temps réel

  En tant que researcher d'emploi
  Je veux être notifié en temps réel des mises à jour
  Afin de ne pas manquer les résultats d'analyse

  # ============================================================================
  # US-009: Connexion SSE
  # ============================================================================

  Scenario: Connexion SSE établie
    Given je suis sur la page "Offres Sauvegardées"
    When la page se charge
    Then une connexion temps réel est établie
    And un indicateur de connexion est affiché (vert)

  Scenario: Déconnexion automatique
    Given la connexion temps réel est établie
    When le serveur se déconnecte
    Then un indicateur de déconnexion est affiché (rouge)
    And une reconnexion automatique est tentée

  # ============================================================================
  # US-009: Notifications temps réel
  # ============================================================================

  Scenario: Notification d'analyse terminée
    Given la connexion temps réel est établie
    And j'ai lancé une analyse sur une offre
    When l'analyse est terminée
    Then une notification "Analyse terminée" s'affiche
    And la carte de l'offre se met à jour avec le statut "Terminé"

  Scenario: Notification d'analyse échouée
    Given la connexion temps réel est établie
    And j'ai lancé une analyse sur une offre
    When l'analyse échoue
    Then une notification "L'analyse a échoué" s'affiche
    And la carte de l'offre affiche le statut "Échoué"

  # ============================================================================
  # US-009: Mise à jour automatique
  # ============================================================================

  Scenario: Mise à jour automatique des statuts
    Given la connexion temps réel est établie
    And plusieurs offres sont en cours d'analyse
    When une analyse se termine
    Then seules les cartes concernées sont mises à jour
    And les autres cartes ne sont pas affectées
