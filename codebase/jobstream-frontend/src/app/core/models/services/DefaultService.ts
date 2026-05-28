/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AnalysisDto } from '../models/AnalysisDto';
import type { CreateAnalysisRequest } from '../models/CreateAnalysisRequest';
import type { SaveJobRequest } from '../models/SaveJobRequest';
import type { HealthResponse } from '../models/HealthResponse';
import type { JobDto } from '../models/JobDto';
import type { JobSearchResponse } from '../models/JobSearchResponse';
import type { PagedSavedJobResponse } from '../models/PagedSavedJobResponse';
import type { SavedJobDto } from '../models/SavedJobDto';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class DefaultService {
    /**
     * Rechercher des offres d'emploi
     * Recherche des offres via l'API JSearch externe.
     * Utiliser les query params pour filtrer.
     *
     * @returns JobSearchResponse Liste des jobs trouvés
     * @throws ApiError
     */
    public static getApiJobs({
        q,
        page = 1,
        limit = 20,
    }: {
        /**
         * Mot-clé de recherche
         */
        q: string,
        /**
         * Numéro de page
         */
        page?: number,
        /**
         * Nombre de résultats par page
         */
        limit?: number,
    }): CancelablePromise<JobSearchResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/jobs',
            query: {
                'q': q,
                'page': page,
                'limit': limit,
            },
            errors: {
                400: `Requête invalide`,
                500: `Erreur serveur interne`,
            },
        });
    }
    /**
     * Détail d'un job externe
     * Retourne les détails d'un job trouvé via JSearch
     * @returns JobDto Détails du job
     * @throws ApiError
     */
    public static getApiJobs1({
        externalId,
    }: {
        /**
         * ID externe du job (JSearch)
         */
        externalId: string,
    }): CancelablePromise<JobDto> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/jobs/{externalId}',
            path: {
                'externalId': externalId,
            },
            errors: {
                404: `Ressource non trouvée`,
            },
        });
    }
    /**
     * Liste des jobs sauvegardés
     * Retourne tous les jobs sauvegardés par l'utilisateur
     * @returns PagedSavedJobResponse Liste paginée des jobs sauvegardés
     * @throws ApiError
     */
    public static getApiSavedJobs({
        page,
        size = 20,
        sort = 'savedAt,desc',
    }: {
        /**
         * Numéro de page
         */
        page?: number,
        /**
         * Taille de page
         */
        size?: number,
        /**
         * Tri (savedAt, title)
         */
        sort?: string,
    }): CancelablePromise<PagedSavedJobResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/saved-jobs',
            query: {
                'page': page,
                'size': size,
                'sort': sort,
            },
            errors: {
                500: `Erreur serveur interne`,
            },
        });
    }
    /**
     * Sauvegarder un job
     * Sauvegarde un job en base de données.
     * Déclenche l'analyse IA automatiquement via Kafka.
     *
     * @returns SavedJobDto Job sauvegardé
     * @throws ApiError
     */
    public static postApiSavedJobs({
        requestBody,
    }: {
        requestBody: SaveJobRequest,
    }): CancelablePromise<SavedJobDto> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/saved-jobs',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Requête invalide`,
                409: `Ressource déjà existante`,
            },
        });
    }
    /**
     * Détail d'un job sauvegardé
     * Retourne les détails d'un job sauvegardé avec son analyse
     * @returns SavedJobDto Détails du job sauvegardé
     * @throws ApiError
     */
    public static getApiSavedJobs1({
        id,
    }: {
        /**
         * ID interne du job sauvegardé
         */
        id: string,
    }): CancelablePromise<SavedJobDto> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/saved-jobs/{id}',
            path: {
                'id': id,
            },
            errors: {
                404: `Ressource non trouvée`,
            },
        });
    }
    /**
     * Supprimer un job sauvegardé
     * Supprime un job sauvegardé de la base de données
     * @returns void
     * @throws ApiError
     */
    public static deleteApiSavedJobs({
        id,
    }: {
        /**
         * ID du job à supprimer
         */
        id: string,
    }): CancelablePromise<void> {
        return __request(OpenAPI, {
            method: 'DELETE',
            url: '/api/saved-jobs/{id}',
            path: {
                'id': id,
            },
            errors: {
                404: `Ressource non trouvée`,
            },
        });
    }
    /**
     * Déclencher une analyse IA
     * Lance l'analyse IA d'un job sauvegardé via Kafka.
     * Retourne immédiatement avec un ID d'analyse.
     *
     * @returns AnalysisDto Analyse acceptée
     * @throws ApiError
     */
    public static postApiAnalyses({
        requestBody,
    }: {
        requestBody: CreateAnalysisRequest,
    }): CancelablePromise<AnalysisDto> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/analyses',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Requête invalide`,
                404: `Ressource non trouvée`,
            },
        });
    }
    /**
     * Liste des analyses
     * Retourne toutes les analyses de l'utilisateur
     * @returns AnalysisDto Liste des analyses
     * @throws ApiError
     */
    public static getApiAnalyses({
        savedJobId,
    }: {
        /**
         * Filtrer par job sauvegardé
         */
        savedJobId?: string,
    }): CancelablePromise<Array<AnalysisDto>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/analyses',
            query: {
                'savedJobId': savedJobId,
            },
        });
    }
    /**
     * Détail d'une analyse
     * Retourne le résultat complet de l'analyse IA
     * @returns AnalysisDto Détails de l'analyse
     * @throws ApiError
     */
    public static getApiAnalyses1({
        id,
    }: {
        /**
         * ID de l'analyse
         */
        id: string,
    }): CancelablePromise<AnalysisDto> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/analyses/{id}',
            path: {
                'id': id,
            },
            errors: {
                404: `Ressource non trouvée`,
            },
        });
    }
    /**
     * Déclencher l'analyse d'un job sauvegardé
     * Raccourci pour lancer l'analyse d'un job spécifique
     * @returns AnalysisDto Analyse acceptée
     * @throws ApiError
     */
    public static postApiSavedJobsAnalyze({
        id,
    }: {
        /**
         * ID du job sauvegardé
         */
        id: string,
    }): CancelablePromise<AnalysisDto> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/saved-jobs/{id}/analyze',
            path: {
                'id': id,
            },
            errors: {
                404: `Ressource non trouvée`,
            },
        });
    }
    /**
     * Flux temps réel des mises à jour
     * Endpoint Server-Sent Events pour recevoir les mises à jour
     * en temps réel (statut analyse, etc.)
     *
     * @returns string Flux SSE
     * @throws ApiError
     */
    public static getApiEvents(): CancelablePromise<string> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/events',
        });
    }
    /**
     * Health check
     * Vérifie que le service est opérationnel
     * @returns HealthResponse Service OK
     * @throws ApiError
     */
    public static getApiHealth(): CancelablePromise<HealthResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/health',
        });
    }
}
