/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
/**
 * DTO interne pour la requête de sauvegarde d'un job
 */
export type SaveJobRequest = {
    externalId: string;
    title: string;
    company: string;
    location: string;
    description?: string;
    contractType?: string;
    postedDate?: string;
    jobUrl?: string;
    requirements?: Array<string>;
    benefits?: Array<string>;
};
