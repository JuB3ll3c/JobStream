import { Injectable, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { LoggerService } from './logger.service';

/**
 * Centralized error handling service
 */
@Injectable({
  providedIn: 'root'
})
export class ErrorHandlerService {
  private readonly logger = inject(LoggerService);

  /**
   * Handle HTTP error and return user-friendly message
   */
  handleHttpError(error: HttpErrorResponse): string {
    this.logger.error('HTTP error occurred', error);

    if (error.status === 0) {
      return 'Connection error. Please check your internet connection.';
    } else if (error.status === 400) {
      return 'Invalid request. Please check your search criteria.';
    } else if (error.status === 401) {
      return 'Authentication required. Please log in.';
    } else if (error.status === 403) {
      return 'Access denied. You do not have the required permissions.';
    } else if (error.status === 404) {
      return 'Resource not found.';
    } else if (error.status >= 500) {
      return 'Server error. Please try again later.';
    }

    return 'An unexpected error occurred.';
  }

  /**
   * Handle generic error and return user-friendly message
   */
  handleGenericError(error: unknown): string {
    this.logger.error('Generic error occurred', error);

    if (error instanceof Error) {
      return `Error: ${error.message}`;
    }

    return 'An unexpected error occurred.';
  }

  /**
   * Check if error is a network error
   */
  isNetworkError(error: unknown): boolean {
    return error instanceof HttpErrorResponse && error.status === 0;
  }

  /**
   * Check if error is a server error
   */
  isServerError(error: unknown): boolean {
    return error instanceof HttpErrorResponse && error.status >= 500;
  }

  /**
   * Check if error is a client error
   */
  isClientError(error: unknown): boolean {
    return error instanceof HttpErrorResponse && error.status >= 400 && error.status < 500;
  }
}
