import { TestBed } from '@angular/core/testing';
import {
  HttpHandlerFn,
  HttpHeaders,
  HttpInterceptorFn,
  HttpRequest,
  HttpResponse,
} from '@angular/common/http';
import { firstValueFrom, of } from 'rxjs';

import { authInterceptor } from './auth-interceptor';
import { AuthService } from './auth-service';

describe('authInterceptor', () => {
  const interceptor: HttpInterceptorFn = (req, next) =>
    TestBed.runInInjectionContext(() => authInterceptor(req, next));
  let authService: { getToken: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    authService = { getToken: vi.fn() };
    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: authService }],
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
