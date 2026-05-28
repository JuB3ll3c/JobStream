import { ApplicationConfig, provideBrowserGlobalErrorListeners, APP_INITIALIZER } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { OpenApiConfig } from './core/config/openapi.config';
import { httpInterceptor } from './core/interceptors/http.interceptor';

/**
 * Initialiseur de l'application
 * Configure le client OpenAPI au démarrage
 */
function initializeApp(): () => void {
  return () => {
    OpenApiConfig.initialize();
  };
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([httpInterceptor])),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      multi: true
    }
  ]
};
