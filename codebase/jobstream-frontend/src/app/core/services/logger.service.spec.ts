import { TestBed } from '@angular/core/testing';
import { LoggerService } from './logger.service';
import { vi } from 'vitest';

describe('LoggerService', () => {
  let service: LoggerService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LoggerService]
    });
    service = TestBed.inject(LoggerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should log info messages only in development', () => {
    const consoleSpy = vi.spyOn(console, 'info');
    service.info('Test message');
    
    // In development, message should be logged
    expect(consoleSpy).toHaveBeenCalledWith('[INFO] Test message');
  });

  it('should log error messages always', () => {
    const consoleSpy = vi.spyOn(console, 'error');
    service.error('Test error');
    
    expect(consoleSpy).toHaveBeenCalledWith('[ERROR] Test error');
  });

  it('should log warn messages always', () => {
    const consoleSpy = vi.spyOn(console, 'warn');
    service.warn('Test warning');
    
    expect(consoleSpy).toHaveBeenCalledWith('[WARN] Test warning');
  });

  it('should handle error with additional parameters', () => {
    const consoleSpy = vi.spyOn(console, 'error');
    const testError = new Error('Test error');
    service.error('Error occurred', testError);
    
    expect(consoleSpy).toHaveBeenCalledWith('[ERROR] Error occurred', testError);
  });
});
