import { TestBed } from '@angular/core/testing';
import { firstValueFrom, of, throwError } from 'rxjs';

import {
  AuthenticationService,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  RoleDto,
} from '../../../generated';
import { AuthService } from './auth-service';

describe('AuthService', () => {
  let service: AuthService;
  let authenticationApi: {
    login: ReturnType<typeof vi.fn>;
    register: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    sessionStorage.clear();
    authenticationApi = { login: vi.fn(), register: vi.fn() };

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
      role: RoleDto.User,
    };
    authenticationApi.login.mockReturnValue(of(response));

    await expect(firstValueFrom(service.login(request))).resolves.toEqual(response);

    expect(authenticationApi.login).toHaveBeenCalledWith({ loginRequest: request });
    expect(sessionStorage.getItem('access_token')).toBe('jwt-token');
    expect(sessionStorage.getItem('token_type')).toBe('Bearer');
    expect(sessionStorage.getItem('role')).toBe('USER');
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
    expect(sessionStorage.getItem('role')).toBeNull();
  });

  it('should register through the generated API and store the authentication response', async () => {
    const request: RegisterRequest = {
      email: 'alice@test.com',
      password: 'password123',
      firstName: 'Alice',
      lastName: 'Dupont',
    };
    const response: AuthResponse = {
      accessToken: 'jwt-token',
      tokenType: 'Bearer',
      role: RoleDto.User,
    };
    authenticationApi.register.mockReturnValue(of(response));

    await expect(firstValueFrom(service.register(request))).resolves.toEqual(response);

    expect(authenticationApi.register).toHaveBeenCalledWith({ registerRequest: request });
    expect(sessionStorage.getItem('access_token')).toBe('jwt-token');
    expect(sessionStorage.getItem('token_type')).toBe('Bearer');
    expect(sessionStorage.getItem('role')).toBe('USER');
  });

  it('should not store authentication data when registration fails', async () => {
    const request: RegisterRequest = {
      email: 'alice@test.com',
      password: 'password123',
      firstName: 'Alice',
      lastName: 'Dupont',
    };
    authenticationApi.register.mockReturnValue(throwError(() => new Error('Registration failed')));

    await expect(firstValueFrom(service.register(request))).rejects.toThrow('Registration failed');

    expect(sessionStorage.getItem('access_token')).toBeNull();
    expect(sessionStorage.getItem('token_type')).toBeNull();
    expect(sessionStorage.getItem('role')).toBeNull();
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

  it('should return the stored role', () => {
    sessionStorage.setItem('role', 'ADMIN');

    expect(service.getRole()).toBe(RoleDto.Admin);
  });

  it('should return null when the stored role is unknown', () => {
    sessionStorage.setItem('role', 'SUPER_ADMIN');

    expect(service.getRole()).toBeNull();
  });

  it('should remove all authentication data on logout', () => {
    sessionStorage.setItem('access_token', 'stored-jwt-token');
    sessionStorage.setItem('token_type', 'Bearer');
    sessionStorage.setItem('role', 'ADMIN');

    service.logout();

    expect(sessionStorage.getItem('access_token')).toBeNull();
    expect(sessionStorage.getItem('token_type')).toBeNull();
    expect(sessionStorage.getItem('role')).toBeNull();
  });
});
