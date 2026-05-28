/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AnalysisDto } from './AnalysisDto';
/**
 * Job sauvegardé en base avec analyse IA
 */
export type SavedJobDto = {
    /**
     * ID interne
     */
    id: string;
    /**
     * ID externe (JSearch)
     */
    externalId: string;
    title: string;
    company: string;
    location?: string;
    description?: string;
    salaryMin?: number | null;
    salaryMax?: number | null;
    contractType?: string | null;
    postedDate?: string | null;
    jobUrl?: string;
    requirements?: Array<string> | null;
    benefits?: Array<string> | null;
    /**
     * Date de sauvegarde
     */
    savedAt: string;
    /**
     * Dernière mise à jour
     */
    updatedAt?: string | null;
    analysis?: AnalysisDto;
};

