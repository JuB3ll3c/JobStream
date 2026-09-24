import { TestBed } from '@angular/core/testing';
import { firstValueFrom, of, throwError } from 'rxjs';

import { AuthenticationService, AuthResponse, LoginRequest } from '../../../generated';
import { AuthService } from './auth-service';

describe('AuthService', () => {
  let service: AuthService;
  let authenticationApi: { login: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    sessionStorage.clear();
    authenticationApi = { login: vi.fn() };

    TestBed.configureTestingModule({
      providers: [{ provide: AuthenticationService, useValue: authenticationApi }],
    });
    service = TestBed.inject(AuthService);
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should login through the generated API and store the authentication response', async () => {
    const request: LoginRequest = {
      email: 'alice@test.com',
      password: 'password123',
    };
    const response: AuthResponse = {
      accessToken: 'jwt-token',
      tokenType: 'Bearer',
    };
    authenticationApi.login.mockReturnValue(of(response));

    await expect(firstValueFrom(service.login(request))).resolves.toEqual(response);

    expect(authenticationApi.login).toHaveBeenCalledWith({ loginRequest: request });
    expect(sessionStorage.getItem('access_token')).toBe('jwt-token');
    expect(sessionStorage.getItem('token_type')).toBe('Bearer');
  });

  it('should not store authentication data when login fails', async () => {
    const request: LoginRequest = {
      email: 'alice@test.com',
      password: 'wrong-password',
    };
    authenticationApi.login.mockReturnValue(throwError(() => new Error('Authentication failed')));

    await expect(firstValueFrom(service.login(request))).rejects.toThrow('Authentication failed');

    expect(sessionStorage.getItem('access_token')).toBeNull();
    expect(sessionStorage.getItem('token_type')).toBeNull();
  });

  it('should report the user as unauthenticated when no token is stored', () => {
    expect(service.getToken()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should return the stored token and report the user as authenticated', () => {
    sessionStorage.setItem('access_token', 'stored-jwt-token');

    expect(service.getToken()).toBe('stored-jwt-token');
    expect(service.isAuthenticated()).toBe(true);
  });
});
