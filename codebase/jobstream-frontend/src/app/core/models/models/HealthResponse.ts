/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type HealthResponse = {
    status?: HealthResponse.status;
    timestamp?: string;
};
export namespace HealthResponse {
    export enum status {
        UP = 'UP',
        DOWN = 'DOWN',
    }
}

