/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AnalysisStatus } from './AnalysisStatus';
/**
 * Résultat de l'analyse IA
 */
export type AnalysisDto = {
    /**
     * ID de l'analyse
     */
    id: string;
    /**
     * ID du job analysé
     */
    savedJobId: string;
    status: AnalysisStatus;
    /**
     * Score de correspondance (0-100)
     */
    score?: number | null;
    /**
     * Résumé de l'analyse
     */
    summary?: string | null;
    /**
     * Points forts identifiés
     */
    strengths?: Array<string> | null;
    /**
     * Points faibles identifiés
     */
    weaknesses?: Array<string> | null;
    /**
     * Recommandations personnalisées
     */
    recommendations?: Array<string> | null;
    /**
     * Tags extraits
     */
    tags?: Array<string> | null;
    /**
     * Début de l'analyse
     */
    createdAt?: string;
    /**
     * Fin de l'analyse
     */
    completedAt?: string | null;
};

