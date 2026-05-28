/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { SavedJobDto } from './SavedJobDto';
/**
 * Réponse paginée pour les jobs sauvegardés
 */
export type PagedSavedJobResponse = {
    content?: Array<SavedJobDto>;
    page?: number;
    size?: number;
    totalElements?: number;
    totalPages?: number;
};

