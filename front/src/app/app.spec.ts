import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { provideRouter, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { routes } from './app.routes';
import { BASE_PATH } from './generated';
import { AuthService } from './core/service/auth/auth-service';

describe('App', () => {
  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter(routes),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: BASE_PATH, useValue: '/api' },
      ],
    }).compileComponents();
  });

  afterEach(() => {
    TestBed.inject(HttpTestingController).verify();
    sessionStorage.clear();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should display the navbar', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const navbar: HTMLElement = fixture.nativeElement.querySelector('app-navbar');
    const routerOutlet: HTMLElement = fixture.nativeElement.querySelector('router-outlet');

    expect(navbar).not.toBeNull();
    expect(
      navbar.compareDocumentPosition(routerOutlet) & Node.DOCUMENT_POSITION_FOLLOWING,
    ).toBeTruthy();
  });
  it('updates the existing navbar after login without refreshing or recreating it', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const navbar = fixture.nativeElement.querySelector('app-navbar');
    expect(navbar.querySelector('a')).toBeNull();
    await TestBed.inject(Router).navigateByUrl('/login');
    await fixture.whenStable();
    for (const [id, value] of [
      ['email', 'alice@example.com'],
      ['password', 'test-password'],
    ]) {
      const input = fixture.nativeElement.querySelector('#' + id) as HTMLInputElement;
      input.value = value;
      input.dispatchEvent(new Event('input'));
    }
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    TestBed.inject(HttpTestingController)
      .expectOne('/api/auth/login')
      .flush({ accessToken: 'test-token', tokenType: 'Bearer', role: 'USER' });
    await fixture.whenStable();
    expect(TestBed.inject(Router).url).toBe('/jobs');
    expect(TestBed.inject(AuthService).isAuthenticated()).toBe(true);
    expect(fixture.nativeElement.querySelector('app-navbar')).toBe(navbar);
    expect(navbar.querySelector('a[href="/jobs"]')).not.toBeNull();
    expect(navbar.querySelector('a[href="/saved-jobs"]')).not.toBeNull();
    navbar.querySelector('button').click();
    await fixture.whenStable();
    expect(TestBed.inject(Router).url).toBe('/login');
    expect(fixture.nativeElement.querySelector('app-navbar')).toBe(navbar);
    expect(navbar.querySelector('a')).toBeNull();
    expect(navbar.querySelector('button')).toBeNull();
    expect(sessionStorage.getItem('access_token')).toBeNull();
  });

  it('shows navigation immediately when a session is restored at startup', async () => {
    sessionStorage.setItem('access_token', 'restored-test-token');
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const navbar = fixture.nativeElement.querySelector('app-navbar');
    expect(navbar.querySelector('a[href="/jobs"]')).not.toBeNull();
    expect(navbar.querySelector('a[href="/saved-jobs"]')).not.toBeNull();
  });

  it('keeps personal navigation hidden when login fails', async () => {
    const fixture = TestBed.createComponent(App);
    await TestBed.inject(Router).navigateByUrl('/login');
    await fixture.whenStable();
    for (const [id, value] of [
      ['email', 'alice@example.com'],
      ['password', 'wrong-password'],
    ]) {
      const input = fixture.nativeElement.querySelector('#' + id) as HTMLInputElement;
      input.value = value;
      input.dispatchEvent(new Event('input'));
    }
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    TestBed.inject(HttpTestingController)
      .expectOne('/api/auth/login')
      .flush({}, { status: 401, statusText: 'Unauthorized' });
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('app-navbar a')).toBeNull();
    expect(TestBed.inject(Router).url).toBe('/login');
  });
});
