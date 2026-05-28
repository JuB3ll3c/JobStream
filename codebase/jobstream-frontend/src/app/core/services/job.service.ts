import { Injectable, inject } from '@angular/core';
import { Observable, from, map, catchError, of } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { DefaultService } from '../models/services/DefaultService';
import {
  JobDto,
  JobSearchResponse,
  SavedJobDto,
  PagedSavedJobResponse,
  SaveJobRequest,
  AnalysisDto,
  CreateAnalysisRequest,
  HealthResponse,
  OpenAPI
} from '../models';
import { LoggerService } from './logger.service';
import { ErrorHandlerService } from './error-handler.service';

/**
 * Service centralisé pour les opérations API
 * Utilise le client OpenAPI généré avec des observables RxJS
 */
@Injectable({
  providedIn: 'root'
})
export class JobService {
  private readonly defaultService = DefaultService;
  private readonly http = inject(HttpClient);
  private readonly logger = inject(LoggerService);
  private readonly errorHandler = inject(ErrorHandlerService);
  private readonly baseUrl = OpenAPI.BASE;

  /**
   * Rechercher des offres d'emploi
   * @param query - Mot-clé de recherche
   * @param page - Numéro de page (défaut: 1)
   * @param limit - Nombre de résultats par page (défaut: 20)
   */
  searchJobs(
    query: string,
    page: number = 1,
    limit: number = 20
  ): Observable<JobSearchResponse> {
    this.logger.debug('Searching jobs', { query, page, limit });

    return from(
      this.defaultService.getApiJobs({ q: query, page, limit })
    ).pipe(
      catchError(error => {
        this.logger.error('Job search failed', error);
        throw new Error(this.errorHandler.handleHttpError(error));
      })
    );
  }

  /**
   * Obtenir le détail d'un job
   */
  getJobById(externalId: string): Observable<JobDto> {
    return from(
      this.defaultService.getApiJobs1({ externalId })
    ).pipe(
      catchError(error => {
        this.logger.error('Get job failed', error);
        throw new Error(this.errorHandler.handleHttpError(error));
      })
    );
  }

  /**
   * Obtenir les jobs sauvegardés
   */
  getSavedJobs(
    page: number = 0,
    size: number = 20,
    sort: string = 'savedAt,desc'
  ): Observable<PagedSavedJobResponse> {
    return from(
      this.defaultService.getApiSavedJobs({ page, size, sort })
    ).pipe(
      catchError(error => {
        this.logger.error('Get saved jobs failed', error);
        throw new Error(this.errorHandler.handleHttpError(error));
      })
    );
  }

  /**
   * Sauvegarder un job
   */
  saveJob(request: SaveJobRequest): Observable<SavedJobDto> {
    return from(
      this.defaultService.postApiSavedJobs({ requestBody: request })
    ).pipe(
      catchError(error => {
        this.logger.error('Save job failed', error);
        throw new Error(this.errorHandler.handleHttpError(error));
      })
    );
  }

  /**
   * Supprimer un job sauvegardé
   */
  deleteSavedJob(id: string): Observable<void> {
    return from(
      this.defaultService.deleteApiSavedJobs({ id })
    ).pipe(
      catchError(error => {
        this.logger.error('Delete saved job failed', error);
        throw new Error(this.errorHandler.handleHttpError(error));
      })
    );
  }

  /**
   * Vérifie quels jobs sont sauvegardés par leur externalIds
   * Retourne la liste des IDs internes (UUID) des jobs sauvegardés
   */
  checkSavedJobs(externalIds: string[]): Observable<string[]> {
    return this.http.post<string[]>(`${this.baseUrl}/api/saved-jobs/check`, externalIds).pipe(
      catchError(error => {
        this.logger.error('Check saved jobs failed', error);
        return of([]);
      })
    );
  }

  /**
   * Déclencher une analyse IA via le endpoint /api/saved-jobs/{id}/analyze
   */
  triggerAnalysis(savedJobId: string): Observable<AnalysisDto> {
    return from(
      this.defaultService.postApiSavedJobsAnalyze({ id: savedJobId })
    ).pipe(
      catchError(error => {
        this.logger.error('Trigger analysis failed', error);
        throw new Error(this.errorHandler.handleHttpError(error));
      })
    );
  }

  /**
   * Obtenir le résultat de l'analyse d'un job sauvegardé
   * Appelle GET /api/saved-jobs/{id}/analysis
   */
  getAnalysisBySavedJobId(savedJobId: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/api/saved-jobs/${savedJobId}/analysis`).pipe(
      catchError(error => {
        this.logger.error('Get analysis by saved job ID failed', error);
        throw new Error(this.errorHandler.handleHttpError(error));
      })
    );
  }

  /**
   * Obtenir une analyse par ID
   */
  getAnalysis(analysisId: string): Observable<AnalysisDto> {
    return from(
      this.defaultService.getApiAnalyses1({ id: analysisId })
    ).pipe(
      catchError(error => {
        this.logger.error('Get analysis failed', error);
        throw new Error(this.errorHandler.handleHttpError(error));
      })
    );
  }

  /**
   * Health check
   */
  healthCheck(): Observable<HealthResponse> {
    return from(
      this.defaultService.getApiHealth()
    ).pipe(
      catchError(error => {
        this.logger.error('Health check failed', error);
        throw new Error(this.errorHandler.handleHttpError(error));
      })
    );
  }
}
