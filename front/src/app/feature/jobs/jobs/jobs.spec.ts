import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { BASE_PATH, JobDto } from '../../../generated';
import { Jobs } from './jobs';
import { Login } from '../../auth/login/login';
import { authInterceptor } from '../../../core/service/auth/auth-interceptor';
import { JobDetail } from '../job-detail/job-detail';

describe('Jobs search', () => {
  let http: HttpTestingController;
  let harness: RouterTestingHarness;
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
  afterEach(() => http.verify());

  const offer: JobDto = {
    externalId: 'ext-1',
    title: 'Java Engineer',
    company: 'Acme',
    location: 'Zurich',
    description: 'An excerpt',
    salaryMin: 80000,
    salaryMax: 100000,
    contractType: 'permanent',
    postedDate: '2026-10-01T00:00:00Z',
    jobUrl: 'https://example.com/jobs/1',
    requirements: ['Java'],
  };

  async function showOffers(content: JobDto[] = [offer]): Promise<void> {
    await harness.navigateByUrl('/jobs?title=Java', Jobs);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({
        content,
        page: 1,
        size: 20,
        totalElements: content.length,
        totalPages: 1,
      });
    harness.detectChanges();
  }

  function saveButton(index = 0): HTMLButtonElement {
    return harness.routeNativeElement!.querySelectorAll<HTMLButtonElement>('.offer-card button')[
      index
    ];
  }

  it('opens the selected offer without refetching and keeps the search URL context', async () => {
    await harness.navigateByUrl('/jobs?title=Java&location=Zurich&page=2&size=10', Jobs);
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
    const link = harness.routeNativeElement!.querySelector<HTMLAnchorElement>('.offer-card h2 a')!;
    expect(link.getAttribute('href')).toBe('/jobs/ext-1?title=Java&location=Zurich&page=2&size=10');
    link.click();
    await harness.fixture.whenStable();
    harness.detectChanges();
    expect(TestBed.inject(Router).url).toBe(
      '/jobs/ext-1?title=Java&location=Zurich&page=2&size=10',
    );
    expect(harness.routeNativeElement!.querySelector('h1')!.textContent).toBe('Java Engineer');
    http.expectNone((req) => req.url === '/api/job-offers' || req.url.startsWith('/api/adzuna/'));
  });

  it('uses an accessible bookmark icon that fills when saved', async () => {
    await showOffers();
    expect(saveButton().getAttribute('aria-label')).toBe('Sauvegarder Java Engineer');
    expect(saveButton().getAttribute('aria-pressed')).toBe('false');
    expect(saveButton().querySelector('svg[aria-hidden="true"]')).not.toBeNull();
    expect(saveButton().textContent!.trim()).toBe('');
    saveButton().click();
    harness.detectChanges();
    expect(saveButton().getAttribute('aria-busy')).toBe('true');
    http.expectOne('/api/jobs').flush({ ...offer, id: 1 }, { status: 201, statusText: 'Created' });
    harness.detectChanges();
    expect(saveButton().getAttribute('aria-pressed')).toBe('true');
    expect(saveButton().getAttribute('aria-label')).toBe('Sauvegardée : Java Engineer');
    expect(saveButton().disabled).toBe(true);
  });

  it('redirects to login when the session expires during a save', async () => {
    await showOffers();
    sessionStorage.setItem('access_token', 'expired-token');
    saveButton().click();
    http.expectOne('/api/jobs').flush({}, { status: 401, statusText: 'Unauthorized' });
    await harness.fixture.whenStable();
    harness.detectChanges();
    expect(TestBed.inject(Router).url).toBe('/login');
    expect(sessionStorage.getItem('access_token')).toBeNull();
    expect(harness.routeNativeElement!.querySelector('#email')).not.toBeNull();
    expect(harness.routeNativeElement!.textContent).not.toContain('Impossible de sauvegarder');
  });

  it('keeps save state associated with the external offer across searches', async () => {
    await showOffers();
    saveButton().click();
    const saving = http.expectOne('/api/jobs');
    await harness.navigateByUrl('/jobs?title=Engineer', Jobs);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({
        content: [{ ...offer, externalId: 'ext-2' }, offer],
        page: 1,
        size: 20,
        totalElements: 2,
        totalPages: 1,
      });
    harness.detectChanges();
    expect(saveButton().disabled).toBe(false);
    expect(saveButton(1).disabled).toBe(true);
    expect(saveButton(1).getAttribute('aria-label')).toContain('Sauvegarde en cours');
    saving.flush({ ...offer, id: 1 }, { status: 201, statusText: 'Created' });
    harness.detectChanges();
    expect(saveButton(1).getAttribute('aria-label')).toContain('Sauvegardée');
    expect(saveButton().getAttribute('aria-label')).toContain('Sauvegarder');
  });

  it('prevents duplicate submissions while allowing another offer to be saved', async () => {
    await showOffers([offer, { ...offer, externalId: 'ext-2' }]);
    saveButton().click();
    saveButton().click();
    harness.detectChanges();
    expect(saveButton().disabled).toBe(true);
    expect(saveButton(1).disabled).toBe(false);
    expect(
      harness.routeNativeElement!.querySelector('.offer-card [role="status"]')!.textContent,
    ).toContain('Sauvegarde en cours');
    saveButton(1).click();
    const requests = http.match('/api/jobs');
    expect(requests.map((request) => request.request.body.externalId)).toEqual(['ext-1', 'ext-2']);
    requests[1].flush(
      { ...offer, externalId: 'ext-2', id: 2 },
      { status: 201, statusText: 'Created' },
    );
    harness.detectChanges();
    expect(saveButton(1).getAttribute('aria-label')).toContain('Sauvegardée');
    expect(saveButton().getAttribute('aria-label')).toContain('Sauvegarde en cours');
    requests[0].flush({ ...offer, id: 1 }, { status: 201, statusText: 'Created' });
    harness.detectChanges();
    expect(saveButton().getAttribute('aria-label')).toContain('Sauvegardée');
  });

  it('shows a technical error only on the failed offer and allows retrying', async () => {
    await showOffers([offer, { ...offer, externalId: 'ext-2', title: 'Another offer' }]);
    saveButton().click();
    http
      .expectOne('/api/jobs')
      .flush({ message: 'internal details' }, { status: 500, statusText: 'Error' });
    harness.detectChanges();
    const cards = harness.routeNativeElement!.querySelectorAll('.offer-card');
    expect(cards[0].querySelector('[role="alert"]')!.textContent).toContain(
      'Impossible de sauvegarder',
    );
    expect(cards[0].textContent).not.toContain('internal details');
    expect(cards[1].querySelector('[role="alert"]')).toBeNull();
    expect(saveButton().disabled).toBe(false);
    saveButton().click();
    harness.detectChanges();
    expect(cards[0].querySelector('[role="alert"]')).toBeNull();
    http.expectOne('/api/jobs').flush({ ...offer, id: 1 }, { status: 201, statusText: 'Created' });
    harness.detectChanges();
    expect(saveButton().getAttribute('aria-label')).toContain('Sauvegardée');
  });

  it('treats a duplicate as already saved rather than a technical error', async () => {
    await showOffers();
    saveButton().click();
    http.expectOne('/api/jobs').flush({}, { status: 409, statusText: 'Conflict' });
    harness.detectChanges();
    expect(saveButton().getAttribute('aria-label')).toContain('Déjà sauvegardée');
    expect(saveButton().disabled).toBe(true);
    expect(harness.routeNativeElement!.querySelector('.offer-card [role="alert"]')).toBeNull();
  });

  it('saves an offer without sending ownership or persistence fields', async () => {
    await showOffers([{ ...offer, id: 99, createdAt: '2026-10-02T00:00:00Z' }]);
    expect(saveButton().getAttribute('aria-label')).toContain('Sauvegarder');
    saveButton().click();
    const request = http.expectOne('/api/jobs');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      externalId: 'ext-1',
      title: 'Java Engineer',
      company: 'Acme',
      location: 'Zurich',
      description: 'An excerpt',
      salaryMin: 80000,
      salaryMax: 100000,
      contractType: 'permanent',
      postedDate: '2026-10-01T00:00:00Z',
      jobUrl: 'https://example.com/jobs/1',
      requirements: ['Java'],
    });
    request.flush({ ...offer, id: 1 }, { status: 201, statusText: 'Created' });
    harness.detectChanges();
    expect(saveButton().getAttribute('aria-label')).toContain('Sauvegardée');
    expect(saveButton().disabled).toBe(true);
  });

  it('reports blank keywords supplied in the URL as invalid criteria', async () => {
    await harness.navigateByUrl('/jobs?title=%20%20', Jobs);
    expect(harness.routeNativeElement!.textContent).toContain('Saisissez un poste');
    http.expectNone((req) => req.url === '/api/job-offers');
  });
  it('allows retrying identical criteria after an error', async () => {
    await harness.navigateByUrl('/jobs?title=Java&page=1&size=20', Jobs);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({}, { status: 503, statusText: 'Unavailable' });
    harness.routeNativeElement!.querySelector('form')!.dispatchEvent(new Event('submit'));
    await harness.fixture.whenStable();
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
    expect(harness.routeNativeElement!.textContent).toContain('Aucune offre');
  });
  it('rejects invalid URL pagination without sending it to the API', async () => {
    await harness.navigateByUrl('/jobs?title=Java&page=0&size=101', Jobs);
    expect(harness.routeNativeElement!.textContent).toContain(
      'paramètres de pagination sont invalides',
    );
    http.expectNone((req) => req.url === '/api/job-offers');
  });
  it('distinguishes invalid server criteria from provider errors', async () => {
    await harness.navigateByUrl('/jobs?title=Java', Jobs);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({}, { status: 400, statusText: 'Bad Request' });
    harness.detectChanges();
    expect(harness.routeNativeElement!.textContent).toContain(
      'critères de recherche sont invalides',
    );
  });
  it('clears the session and redirects directly to login on 401', async () => {
    sessionStorage.setItem('access_token', 'expired-token');
    await harness.navigateByUrl('/jobs?title=Java', Jobs);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({}, { status: 401, statusText: 'Unauthorized' });
    await harness.fixture.whenStable();
    harness.detectChanges();
    expect(TestBed.inject(Router).url).toBe('/login');
    expect(sessionStorage.getItem('access_token')).toBeNull();
    expect(harness.routeNativeElement!.querySelector('#email')).not.toBeNull();
    expect(harness.routeNativeElement!.textContent).not.toContain('session a expiré');
  });
  it('reports provider unavailability and allows another search afterwards', async () => {
    await harness.navigateByUrl('/jobs?title=Java', Jobs);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({}, { status: 503, statusText: 'Unavailable' });
    harness.detectChanges();
    expect(harness.routeNativeElement!.textContent).toContain('temporairement indisponible');
    await harness.navigateByUrl('/jobs?title=Python', Jobs);
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
    expect(harness.routeNativeElement!.textContent).not.toContain('temporairement indisponible');
  });
  it('announces loading and cancels obsolete searches when the URL changes', async () => {
    await harness.navigateByUrl('/jobs?title=Java', Jobs);
    const old = http.expectOne((req) => req.url === '/api/job-offers');
    expect(harness.routeNativeElement!.textContent).toContain('Chargement');
    await harness.navigateByUrl('/jobs?title=Python', Jobs);
    expect(old.cancelled).toBe(true);
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
    expect(harness.routeNativeElement!.textContent).not.toContain('Chargement');
  });
  it('shows results and changes page without losing criteria', async () => {
    await harness.navigateByUrl('/jobs?title=Java&location=Zurich&page=1&size=20', Jobs);
    http
      .expectOne((req) => req.url === '/api/job-offers')
      .flush({
        content: [
          { externalId: 'ext-1', title: 'Backend Engineer', company: 'Acme', location: 'Zurich' },
        ],
        page: 1,
        size: 20,
        totalElements: 21,
        totalPages: 2,
      });
    harness.detectChanges();
    expect(harness.routeNativeElement!.textContent).toContain('Backend Engineer');
    expect(harness.routeNativeElement!.textContent).toContain('Acme');
    expect(
      harness.routeNativeElement!.querySelector<HTMLButtonElement>(
        '[aria-label="Page précédente"]',
      )!.disabled,
    ).toBe(true);
    harness
      .routeNativeElement!.querySelector<HTMLButtonElement>('[aria-label="Page suivante"]')!
      .click();
    await harness.fixture.whenStable();
    expect(TestBed.inject(Router).url).toBe('/jobs?title=Java&location=Zurich&page=2&size=20');
    const request = http.expectOne((req) => req.url === '/api/job-offers');
    expect(request.request.params.get('page')).toBe('2');
    request.flush({ content: [], page: 2, size: 20, totalElements: 21, totalPages: 2 });
    harness.detectChanges();
    expect(
      harness.routeNativeElement!.querySelector<HTMLButtonElement>('[aria-label="Page suivante"]')!
        .disabled,
    ).toBe(true);
  });
  it('rejects blank keywords without navigating or requesting offers', async () => {
    await harness.navigateByUrl('/jobs', Jobs);
    const input = harness.routeNativeElement!.querySelector<HTMLInputElement>('#title')!;
    input.value = '   ';
    input.dispatchEvent(new Event('input'));
    harness.routeNativeElement!.querySelector('form')!.dispatchEvent(new Event('submit'));
    await harness.fixture.whenStable();
    harness.detectChanges();
    expect(TestBed.inject(Router).url).toBe('/jobs');
    expect(input.getAttribute('aria-invalid')).toBe('true');
    expect(harness.routeNativeElement!.textContent).toContain('Saisissez un poste');
    http.expectNone((req) => req.url === '/api/job-offers');
  });
  it('starts without a request and submits trimmed criteria on page one', async () => {
    await harness.navigateByUrl('/jobs', Jobs);
    http.expectNone((req) => req.url === '/api/job-offers');
    expect(harness.routeNativeElement!.textContent).toContain('Lancez une recherche');
    for (const [id, value] of [
      ['title', ' Java '],
      ['location', ' Zurich '],
    ]) {
      const input = harness.routeNativeElement!.querySelector<HTMLInputElement>(`#${id}`)!;
      input.value = value;
      input.dispatchEvent(new Event('input'));
    }
    harness.routeNativeElement!.querySelector('form')!.dispatchEvent(new Event('submit'));
    await harness.fixture.whenStable();
    const request = http.expectOne((req) => req.url === '/api/job-offers');
    expect(TestBed.inject(Router).url).toBe('/jobs?title=Java&location=Zurich&page=1&size=20');
    request.flush({ content: [], page: 1, size: 20, totalElements: 0, totalPages: 0 });
  });
  it('loads the search criteria and page from the URL', async () => {
    await harness.navigateByUrl('/jobs?title=Java&location=Zurich&page=2&size=20', Jobs);
    const request = http.expectOne((req) => req.url === '/api/job-offers');
    expect(request.request.params.get('title')).toBe('Java');
    expect(request.request.params.get('location')).toBe('Zurich');
    expect(request.request.params.get('page')).toBe('2');
    request.flush({ content: [], page: 2, size: 20, totalElements: 0, totalPages: 0 });
    harness.detectChanges();
    expect(harness.routeNativeElement!.textContent).toContain('Aucune offre');
    expect(harness.routeNativeElement!.querySelector<HTMLInputElement>('#title')!.value).toBe(
      'Java',
    );
  });
});
