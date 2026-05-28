/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
/**
 * Job trouvé via l'API externe (non sauvegardé)
 */
export type JobDto = {
    /**
     * ID externe (JSearch)
     */
    externalId: string;
    title: string;
    company: string;
    location: string;
    /**
     * Description complète de l'offre
     */
    description?: string;
    /**
     * Salaire minimum
     */
    salaryMin?: number | null;
    /**
     * Salaire maximum
     */
    salaryMax?: number | null;
    /**
     * Type de contrat
     */
    contractType?: string | null;
    /**
     * Date de publication
     */
    postedDate?: string | null;
    /**
     * URL originale de l'offre
     */
    jobUrl?: string;
    /**
     * Liste des exigences
     */
    requirements?: Array<string> | null;
    /**
     * Avantages proposés
     */
    benefits?: Array<string> | null;
};

