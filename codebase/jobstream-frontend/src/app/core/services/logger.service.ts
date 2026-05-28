import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

/**
 * Service de logging centralisé pour remplacer console.log/error
 * Respecte la règle TS003: No console.log in production
 */
@Injectable({
  providedIn: 'root'
})
export class LoggerService {
  /**
   * Log un message d'information (seulement en développement)
   */
  info(message: string, ...optionalParams: unknown[]): void {
    if (!environment.production) {
      console.info(`[INFO] ${message}`, ...optionalParams);
    }
  }

  /**
   * Log un message de debug (seulement en développement)
   */
  debug(message: string, ...optionalParams: unknown[]): void {
    if (!environment.production) {
      console.debug(`[DEBUG] ${message}`, ...optionalParams);
    }
  }

  /**
   * Log un message d'erreur (toujours loggé, même en production)
   */
  error(message: string, error?: unknown): void {
    // En production, on devrait envoyer à un service de monitoring
    console.error(`[ERROR] ${message}`, error);
    
    // TODO: Intégrer avec un service de monitoring (Sentry, etc.)
    if (environment.production) {
      // Envoyer l'erreur au serveur de monitoring
      this.sendToMonitoring(message, error);
    }
  }

  /**
   * Log un message d'avertissement
   */
  warn(message: string, ...optionalParams: unknown[]): void {
    console.warn(`[WARN] ${message}`, ...optionalParams);
  }

  private sendToMonitoring(message: string, error?: unknown): void {
    // Implémentation pour envoyer les erreurs à un service de monitoring
    // Ex: Sentry, LogRocket, etc.
    // Pour l'instant, c'est un placeholder
    try {
      // Simuler l'envoi à un service externe
      if (navigator.sendBeacon) {
        const data = new Blob([JSON.stringify({
          message,
          error: error instanceof Error ? error.message : String(error),
          timestamp: new Date().toISOString(),
          url: window.location.href
        })], { type: 'application/json' });
        navigator.sendBeacon('/api/monitoring/errors', data);
      }
    } catch (monitoringError) {
      // Ne pas créer une boucle infinie d'erreurs
      console.error('Failed to send error to monitoring:', monitoringError);
    }
  }
}