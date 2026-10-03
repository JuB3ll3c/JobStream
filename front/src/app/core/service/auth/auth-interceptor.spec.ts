import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import {
  HttpHandlerFn,
  HttpClient,
  HttpHeaders,
  HttpInterceptorFn,
  HttpRequest,
  HttpResponse,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { firstValueFrom, of } from 'rxjs';

import { authInterceptor } from './auth-interceptor';
import { AuthService } from './auth-service';
import { Login } from '../../../feature/auth/login/login';

describe('authInterceptor', () => {
  const interceptor: HttpInterceptorFn = (req, next) =>
    TestBed.runInInjectionContext(() => authInterceptor(req, next));
  let authService: { getToken: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    authService = { getToken: vi.fn() };
    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: authService }, provideRouter([])],
    });
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });

  it('should add a Bearer token to a protected backend request', async () => {
    authService.getToken.mockReturnValue('jwt-token');
    const request = new HttpRequest('GET', '/api/jobs');
    let forwardedRequest: HttpRequest<unknown> | undefined;
    const next: HttpHandlerFn = (nextRequest) => {
      forwardedRequest = nextRequest;
      return of(new HttpResponse());
    };

    await firstValueFrom(interceptor(request, next));

    expect(forwardedRequest?.headers.get('Authorization')).toBe('Bearer jwt-token');
  });

  it('should leave a protected backend request unchanged when no token is available', async () => {
    authService.getToken.mockReturnValue(null);
    const request = new HttpRequest('GET', '/api/jobs');
    let forwardedRequest: HttpRequest<unknown> | undefined;
    const next: HttpHandlerFn = (nextRequest) => {
      forwardedRequest = nextRequest;
      return of(new HttpResponse());
    };

    await firstValueFrom(interceptor(request, next));

    expect(forwardedRequest).toBe(request);
    expect(forwardedRequest?.headers.has('Authorization')).toBe(false);
  });

  it.each(['/api/auth/login', '/api/auth/register'])(
    'should not add an Authorization header to the public endpoint %s',
    async (url) => {
      authService.getToken.mockReturnValue('jwt-token');
      const request = new HttpRequest('POST', url, null);
      let forwardedRequest: HttpRequest<unknown> | undefined;
      const next: HttpHandlerFn = (nextRequest) => {
        forwardedRequest = nextRequest;
        return of(new HttpResponse());
      };

      await firstValueFrom(interceptor(request, next));

      expect(forwardedRequest).toBe(request);
      expect(forwardedRequest?.headers.has('Authorization')).toBe(false);
      expect(authService.getToken).not.toHaveBeenCalled();
    },
  );

  it('should leave an external request and its Authorization header unchanged', async () => {
    authService.getToken.mockReturnValue('jobstream-token');
    const request = new HttpRequest('GET', 'https://partner.example/jobs', {
      headers: new HttpHeaders({ Authorization: 'Basic partner-credentials' }),
    });
    let forwardedRequest: HttpRequest<unknown> | undefined;
    const next: HttpHandlerFn = (nextRequest) => {
      forwardedRequest = nextRequest;
      return of(new HttpResponse());
    };

    await firstValueFrom(interceptor(request, next));

    expect(forwardedRequest).toBe(request);
    expect(forwardedRequest?.headers.get('Authorization')).toBe('Basic partner-credentials');
    expect(authService.getToken).not.toHaveBeenCalled();
  });
});

describe('authInterceptor session handling', () => {
  let http: HttpTestingController;
  let client: HttpClient;
  let router: Router;
  let harness: RouterTestingHarness;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([{ path: 'login', component: Login }]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    client = TestBed.inject(HttpClient);
    router = TestBed.inject(Router);
    harness = await RouterTestingHarness.create();
    sessionStorage.setItem('access_token', 'expired-token');
    sessionStorage.setItem('token_type', 'Bearer');
    sessionStorage.setItem('role', 'USER');
  });

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
  });

  it.each([true, false])(
    'redirects on a protected 401 with token present: %s',
    async (hasToken) => {
      if (!hasToken) sessionStorage.removeItem('access_token');
      let completed = false;
      const errors: unknown[] = [];
      client.get('/api/jobs').subscribe({
        complete: () => {
          completed = true;
        },
        error: (error) => errors.push(error),
      });
      http.expectOne('/api/jobs').flush({}, { status: 401, statusText: 'Unauthorized' });
      await harness.fixture.whenStable();
      expect(router.url).toBe('/login');
      expect(TestBed.inject(AuthService).isAuthenticated()).toBe(false);
      expect(sessionStorage.getItem('token_type')).toBeNull();
      expect(sessionStorage.getItem('role')).toBeNull();
      expect(completed).toBe(true);
      expect(errors).toEqual([]);
    },
  );

  it.each([
    ['/api/auth/login', 401],
    ['/api/auth/register', 401],
    ['https://partner.example/jobs', 401],
    ['/api/job-offers', 403],
    ['/api/job-offers', 503],
  ])('preserves the session and propagates %s errors with status %s', async (url, status) => {
    const response = firstValueFrom(client.get(url));
    const assertion = expect(response).rejects.toMatchObject({ status });
    http.expectOne(url).flush({}, { status, statusText: 'Error' });
    await assertion;
    expect(router.url).toBe('/');
    expect(sessionStorage.getItem('access_token')).toBe('expired-token');
    expect(sessionStorage.getItem('token_type')).toBe('Bearer');
    expect(sessionStorage.getItem('role')).toBe('USER');
  });
});
