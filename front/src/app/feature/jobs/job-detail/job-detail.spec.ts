import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { BASE_PATH, JobDto } from '../../../generated';
import { authInterceptor } from '../../../core/service/auth/auth-interceptor';
import { Login } from '../../auth/login/login';
import { Jobs } from '../jobs/jobs';
import { JobDetail } from './job-detail';

describe('External job detail', () => {
  let http: HttpTestingController;
  let harness: RouterTestingHarness;
  const offer: JobDto = {
    externalId: 'ext-1',
    title: 'Java Engineer',
    company: 'Acme',
    location: 'Zurich',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        provideRouter([
          { path: 'jobs', component: Jobs },
          { path: 'jobs/:externalId', component: JobDetail },
          { path: 'login', component: Login },
        ]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: BASE_PATH, useValue: '/api' },
      ],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
    harness = await RouterTestingHarness.create();
  });

  afterEach(() => {
    try {
      http.verify();
    } finally {
      sessionStorage.clear();
      TestBed.resetTestingModule();
    }
  });

  it('cancels obsolete detail retrieval when the route changes', async () => {
    await harness.navigateByUrl('/jobs/ext-1?title=Java', JobDetail);
    const oldRequest = http.expectOne((req) => req.url === '/api/job-offers');
    await harness.navigateByUrl('/jobs/ext-2?title=Python', JobDetail);
    expect(oldRequest.cancelled).toBe(true);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({
        content: [{ ...offer, externalId: 'ext-2', title: 'Python Engineer' }],
        page: 1,
        size: 20,
        totalElements: 1,
        totalPages: 1,
      });
    harness.detectChanges();
    expect(harness.routeNativeElement!.querySelector('h1')!.textContent).toBe('Python Engineer');
  });

  it('redirects to login when the recovery request returns 401', async () => {
    sessionStorage.setItem('access_token', 'expired-token');
    await harness.navigateByUrl('/jobs/ext-1?title=Java', JobDetail);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({}, { status: 401, statusText: 'Unauthorized' });
    await harness.fixture.whenStable();
    harness.detectChanges();
    expect(TestBed.inject(Router).url).toBe('/login');
    expect(sessionStorage.getItem('access_token')).toBeNull();
    expect(harness.routeNativeElement!.querySelector('#email')).not.toBeNull();
  });

  it('ignores a navigation snapshot that belongs to another offer', async () => {
    await TestBed.inject(Router).navigate(['/jobs', 'ext-2'], {
      queryParams: { title: 'Java' },
      state: { offer },
    });
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({
        content: [],
        page: 1,
        size: 20,
        totalElements: 0,
        totalPages: 0,
      });
    harness.detectChanges();
    expect(harness.routeNativeElement!.textContent).not.toContain('Java Engineer');
    expect(harness.routeNativeElement!.textContent).toContain(
      'plus disponible dans cette recherche',
    );
  });

  it('recovers normally on another URL after a provider error', async () => {
    await harness.navigateByUrl('/jobs/ext-1?title=Java', JobDetail);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({}, { status: 503, statusText: 'Unavailable' });
    await harness.navigateByUrl('/jobs/ext-1?title=Engineer', JobDetail);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({
        content: [offer],
        page: 1,
        size: 20,
        totalElements: 1,
        totalPages: 1,
      });
    harness.detectChanges();
    expect(harness.routeNativeElement!.textContent).not.toContain('temporairement indisponible');
    expect(harness.routeNativeElement!.querySelector('h1')!.textContent).toBe('Java Engineer');
  });

  it('falls back to search when the navigation offer is incomplete', async () => {
    await TestBed.inject(Router).navigate(['/jobs', 'ext-1'], {
      queryParams: { title: 'Java' },
      state: { offer: { externalId: 'ext-1' } },
    });
    const request = http.expectOne((req) => req.url === '/api/job-offers');
    request.flush({ content: [offer], page: 1, size: 20, totalElements: 1, totalPages: 1 });
    harness.detectChanges();
    expect(harness.routeNativeElement!.querySelector('h1')!.textContent).toBe('Java Engineer');
  });

  it('reloads the detail when another offer is opened on the same route', async () => {
    await harness.navigateByUrl('/jobs/ext-1?title=Java', JobDetail);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({
        content: [offer],
        page: 1,
        size: 20,
        totalElements: 1,
        totalPages: 1,
      });
    await harness.navigateByUrl('/jobs/ext-2?title=Python', JobDetail);
    expect(harness.routeNativeElement!.textContent).not.toContain('Java Engineer');
    const request = http.expectOne((req) => req.url === '/api/job-offers');
    expect(request.request.params.get('title')).toBe('Python');
    request.flush({
      content: [{ ...offer, externalId: 'ext-2', title: 'Python Engineer' }],
      page: 1,
      size: 20,
      totalElements: 1,
      totalPages: 1,
    });
    harness.detectChanges();
    expect(harness.routeNativeElement!.querySelector('h1')!.textContent).toBe('Python Engineer');
  });

  it('shows all available details and links to the original advertisement', async () => {
    await harness.navigateByUrl('/jobs/ext-1?title=Java', JobDetail);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({
        content: [
          {
            ...offer,
            description: 'A short excerpt',
            postedDate: '2026-10-01',
            contractType: 'permanent',
            salaryMin: 80000,
            salaryMax: 100000,
            requirements: ['Java', 'Spring'],
            jobUrl: 'https://example.com/job/1',
          },
        ],
        page: 1,
        size: 20,
        totalElements: 1,
        totalPages: 1,
      });
    harness.detectChanges();
    const text = harness.routeNativeElement!.textContent;
    for (const value of [
      'A short excerpt',
      '01/10/2026',
      'permanent',
      '80000',
      '100000',
      'Spring',
      'extrait',
    ]) {
      expect(text).toContain(value);
    }
    const link = harness.routeNativeElement!.querySelector<HTMLAnchorElement>(
      'a[href="https://example.com/job/1"]',
    )!;
    expect(link.textContent).toContain('Voir l’annonce originale');
    expect(link.rel).toContain('noopener');
  });

  it.each(['page=0', 'page=1.5', 'page=abc', 'size=101', 'size=0'])(
    'rejects invalid pagination in a direct link: %s',
    async (pagination) => {
      await harness.navigateByUrl(`/jobs/ext-1?title=Java&${pagination}`, JobDetail);
      expect(harness.routeNativeElement!.textContent).toContain(
        'contexte de recherche est invalide',
      );
      http.expectNone(() => true);
    },
  );

  it('shows a generic provider error without leaking response details', async () => {
    await harness.navigateByUrl('/jobs/ext-1?title=Java', JobDetail);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush(
        { message: 'provider secret' },
        {
          status: 503,
          statusText: 'Unavailable',
        },
      );
    harness.detectChanges();
    expect(harness.routeNativeElement!.textContent).toContain('temporairement indisponible');
    expect(harness.routeNativeElement!.textContent).not.toContain('provider secret');
    expect(harness.routeNativeElement!.textContent).not.toContain('Chargement');
  });

  it('does not mistake another search result for the requested offer', async () => {
    await harness.navigateByUrl('/jobs/missing?title=Java', JobDetail);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({
        content: [offer],
        page: 1,
        size: 20,
        totalElements: 1,
        totalPages: 1,
      });
    harness.detectChanges();
    expect(harness.routeNativeElement!.textContent).toContain(
      'Cette offre n’est plus disponible dans cette recherche',
    );
    expect(harness.routeNativeElement!.textContent).not.toContain('Java Engineer');
  });

  it('explains that a search context is needed for a bare detail URL', async () => {
    await harness.navigateByUrl('/jobs/ext-1', JobDetail);
    expect(harness.routeNativeElement!.textContent).toContain(
      'Ouvrez cette offre depuis une recherche',
    );
    http.expectNone(() => true);
    expect(harness.routeNativeElement!.querySelector('a[href="/jobs"]')).not.toBeNull();
  });

  it('returns to the same search criteria and page', async () => {
    await harness.navigateByUrl('/jobs/ext-1?title=Java&location=Zurich&page=2&size=10', JobDetail);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({
        content: [offer],
        page: 2,
        size: 10,
        totalElements: 11,
        totalPages: 2,
      });
    harness.detectChanges();
    harness
      .routeNativeElement!.querySelector<HTMLAnchorElement>('[data-testid="back-to-search"]')!
      .click();
    await harness.fixture.whenStable();
    expect(TestBed.inject(Router).url).toBe('/jobs?title=Java&location=Zurich&page=2&size=10');
    const request = http.expectOne((req) => req.url === '/api/job-offers');
    expect(request.request.params.get('page')).toBe('2');
    request.flush({ content: [offer], page: 2, size: 10, totalElements: 11, totalPages: 2 });
  });

  it('recovers a directly opened offer from its original search page', async () => {
    await harness.navigateByUrl('/jobs/ext-1?title=Java&location=Zurich&page=2&size=10', JobDetail);
    expect(harness.routeNativeElement!.textContent).toContain('Chargement');
    const request = http.expectOne((req) => req.url === '/api/job-offers');
    expect(request.request.params.get('title')).toBe('Java');
    expect(request.request.params.get('location')).toBe('Zurich');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('10');
    request.flush({ content: [offer], page: 2, size: 10, totalElements: 11, totalPages: 2 });
    harness.detectChanges();
    expect(harness.routeNativeElement!.querySelector('h1')!.textContent).toBe('Java Engineer');
    expect(harness.routeNativeElement!.textContent).toContain('Acme');
    expect(harness.routeNativeElement!.textContent).toContain('Zurich');
  });
});
