import { OpenAPI } from '../models/core/OpenAPI';
import { environment } from '../../../environments/environment';

/**
 * Configuration centralisée du client OpenAPI
 * Initialise la connexion à l'API avec les paramètres d'environnement
 */
export class OpenApiConfig {
  /**
   * Initialise la configuration OpenAPI
   * À appeler au démarrage de l'application
   */
  static initialize(): void {
    // En dev : utiliser le proxy (URL relative)
    // En prod : utiliser l'URL explicite ou variable d'environnement
    const baseUrl = environment.apiBaseUrl;
    
    // Forcer BASE à vide en dev pour utiliser le proxy
    // En prod, si apiBaseUrl est défini, l'utiliser
    if (!environment.production || !baseUrl) {
      // Utiliser des URLs relatives qui passent par le proxy
      OpenAPI.BASE = '';
    } else {
      OpenAPI.BASE = baseUrl;
    }

    // Configuration des credentials
    OpenAPI.CREDENTIALS = 'include';
  }
}
