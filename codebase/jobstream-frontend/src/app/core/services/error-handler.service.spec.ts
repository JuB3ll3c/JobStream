import { TestBed } from '@angular/core/testing';
import { ErrorHandlerService } from './error-handler.service';
import { HttpErrorResponse } from '@angular/common/http';

describe('ErrorHandlerService', () => {
  let service: ErrorHandlerService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ErrorHandlerService]
    });
    service = TestBed.inject(ErrorHandlerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should handle network error (status 0)', () => {
    const error = new HttpErrorResponse({ status: 0 });
    const result = service.handleHttpError(error);
    
    expect(result).toBe('Connection error. Please check your internet connection.');
  });

  it('should handle 400 error', () => {
    const error = new HttpErrorResponse({ status: 400 });
    const result = service.handleHttpError(error);
    
    expect(result).toBe('Invalid request. Please check your search criteria.');
  });

  it('should handle 401 error', () => {
    const error = new HttpErrorResponse({ status: 401 });
    const result = service.handleHttpError(error);
    
    expect(result).toBe('Authentication required. Please log in.');
  });

  it('should handle 403 error', () => {
    const error = new HttpErrorResponse({ status: 403 });
    const result = service.handleHttpError(error);
    
    expect(result).toBe('Access denied. You do not have the required permissions.');
  });

  it('should handle 404 error', () => {
    const error = new HttpErrorResponse({ status: 404 });
    const result = service.handleHttpError(error);
    
    expect(result).toBe('Resource not found.');
  });

  it('should handle 500 error', () => {
    const error = new HttpErrorResponse({ status: 500 });
    const result = service.handleHttpError(error);
    
    expect(result).toBe('Server error. Please try again later.');
  });

  it('should handle generic error', () => {
    const error = new Error('Test error');
    const result = service.handleGenericError(error);
    
    expect(result).toBe('Error: Test error');
  });

  it('should handle unknown error', () => {
    const result = service.handleGenericError('Unknown error');
    
    expect(result).toBe('An unexpected error occurred.');
  });

  it('should identify network error', () => {
    const error = new HttpErrorResponse({ status: 0 });
    const result = service.isNetworkError(error);
    
    expect(result).toBe(true);
  });

  it('should identify server error', () => {
    const error = new HttpErrorResponse({ status: 500 });
    const result = service.isServerError(error);
    
    expect(result).toBe(true);
  });

  it('should identify client error', () => {
    const error = new HttpErrorResponse({ status: 400 });
    const result = service.isClientError(error);
    
    expect(result).toBe(true);
  });
});
