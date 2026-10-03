import { authGuard } from './core/service/auth/auth-guard';
import { Register } from './feature/auth/register/register/register';
import { Jobs } from './feature/jobs/jobs/jobs';
import { routes } from './app.routes';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { BASE_PATH } from './generated';
import { JobDetail } from './feature/jobs/job-detail/job-detail';

describe('application routes', () => {
  afterEach(() => sessionStorage.clear());

  it('redirects an anonymous detail visitor to login before making a search request', async () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter(routes),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: BASE_PATH, useValue: '/api' },
      ],
    });
    sessionStorage.clear();
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/jobs/ext-1?title=Java');
    expect(TestBed.inject(Router).url).toBe('/login');
    const http = TestBed.inject(HttpTestingController);
    http.expectNone(() => true);
    http.verify();
  });

  it('allows an authenticated user to open the external detail route', async () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter(routes),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: BASE_PATH, useValue: '/api' },
      ],
    });
    sessionStorage.setItem('access_token', 'valid-token');
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/jobs/ext-1?title=Java', JobDetail);
    const http = TestBed.inject(HttpTestingController);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({
        content: [],
        page: 1,
        size: 20,
        totalElements: 0,
        totalPages: 0,
      });
    http.verify();
  });
  it('should use the protected jobs page as the default destination', async () => {
    const rootRoute = routes.find((route) => route.path === '');
    const loginRoute = routes.find((route) => route.path === 'login');
    const jobsRoute = routes.find((route) => route.path === 'jobs');

    expect(rootRoute).toEqual({
      path: '',
      redirectTo: 'jobs',
      pathMatch: 'full',
    });
    expect(loginRoute?.canActivate).toBeUndefined();
    expect(jobsRoute?.canActivate).toEqual([authGuard]);
    await expect(jobsRoute?.loadComponent?.()).resolves.toBe(Jobs);
  });

  it('should expose the registration page without authentication', async () => {
    const registerRoute = routes.find((route) => route.path === 'register');

    expect(registerRoute).toBeDefined();
    expect(registerRoute?.canActivate).toBeUndefined();
    await expect(registerRoute?.loadComponent?.()).resolves.toBe(Register);
  });
});
