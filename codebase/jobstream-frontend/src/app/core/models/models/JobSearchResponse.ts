/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { JobDto } from './JobDto';
export type JobSearchResponse = {
    jobs?: Array<JobDto>;
    /**
     * Nombre total de résultats
     */
    total?: number;
    /**
     * Nombre de résultats retournés
     */
    count?: number;
};

