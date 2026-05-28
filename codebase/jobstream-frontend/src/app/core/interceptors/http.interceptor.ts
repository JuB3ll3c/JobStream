import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { LoggerService } from '../services/logger.service';
import { ErrorHandlerService } from '../services/error-handler.service';

/**
 * Intercepteur HTTP pour:
 * - Gestion globale des erreurs
 * - Logging des requêtes
 */
export const httpInterceptor: HttpInterceptorFn = (req, next) => {
  const logger = inject(LoggerService);
  const errorHandler = inject(ErrorHandlerService);

  // Log la requête
  logger.debug(`HTTP ${req.method} ${req.url}`);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Log l'erreur
      logger.error(`HTTP Error ${error.status}: ${error.message}`, {
        url: req.url,
        method: req.method,
        status: error.status
      });

      // Transformation de l'erreur
      const errorMessage = errorHandler.handleHttpError(error);
      
      return throwError(() => new Error(errorMessage));
    })
  );
};
