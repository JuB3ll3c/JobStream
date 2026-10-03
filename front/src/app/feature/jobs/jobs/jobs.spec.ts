import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { BASE_PATH } from '../../../generated';
import { Jobs } from './jobs';
import { Login } from '../../auth/login/login';
import { authInterceptor } from '../../../core/service/auth/auth-interceptor';

describe('Jobs search', () => {
  let http: HttpTestingController;
  let harness: RouterTestingHarness;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        provideRouter([
          { path: 'jobs', component: Jobs },
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
