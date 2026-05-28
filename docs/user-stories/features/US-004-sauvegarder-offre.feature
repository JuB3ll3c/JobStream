# Language: fr

Feature: Sauvegarder une offre d'emploi

  En tant que chercheur d'emploi
  Je veux sauvegarder une offre qui m'intéresse
  Afin de la retrouver facilement plus tard

  # ============================================================================
  # US-004: Sauvegarde basique
  # ============================================================================

  Scenario: Sauvegarder une offre depuis la liste de recherche
    Given je suis sur la page de recherche avec des résultats
    And l'offre "Développeur Java" de l'entreprise "TechCorp" est dans la liste
    When je clique sur le bouton "Sauvegarder" de cette offre
    Then l'offre est enregistrée en base de données
    And un message "Offre sauvegardée" s'affiche
    And le bouton devient "Sauvegardé"

  Scenario: Sauvegarder une offre depuis la page de détails
    Given je suis sur la page de détails d'une offre
    And cette offre n'est pas encore sauvegardée
    When je clique sur le bouton "Sauvegarder"
    Then l'offre est enregistrée en base de données
    And un message "Offre sauvegardée" s'affiche

  # ============================================================================
  # US-004: Cas limites
  # ============================================================================

  Scenario: Tentative de sauvegarde d'une offre déjà sauvegardée
    Given l'offre "Développeur Java" est déjà sauvegardée
    When je clique sur le bouton "Sauvegarder" pour cette offre
    Then un message "Cette offre est déjà sauvegardée" est affiché
    And le bouton reste "Sauvegardé"

  Scenario: Erreur lors de la sauvegarde
    Given je suis sur la page de détails d'une offre
    And le service de sauvegarde est indisponible
    When je clique sur le bouton "Sauvegarder"
    Then un message d'erreur "Impossible de sauvegarder l'offre" est affiché
    And l'offre n'est pas sauvegardée
