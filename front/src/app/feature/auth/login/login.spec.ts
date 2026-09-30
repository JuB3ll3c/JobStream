import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { NEVER, of, throwError } from 'rxjs';

import { AuthService } from '../../../core/service/auth/auth-service';
import { Login } from './login';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let authService: { login: ReturnType<typeof vi.fn> };
  let router: Router;

  beforeEach(async () => {
    authService = { login: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [{ provide: AuthService, useValue: authService }, provideRouter([])],
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not call the authentication service and should touch all fields when the form is invalid', () => {
    component.login();

    expect(authService.login).not.toHaveBeenCalled();
    expect(component.email.touched).toBe(true);
    expect(component.password.touched).toBe(true);
  });

  it('should submit the credentials through the authentication service when the form is valid', () => {
    authService.login.mockReturnValue(NEVER);
    component.email.setValue('alice@test.com');
    component.password.setValue('password123');

    component.login();

    expect(authService.login).toHaveBeenCalledOnce();
    expect(authService.login).toHaveBeenCalledWith({
      email: 'alice@test.com',
      password: 'password123',
    });
  });

  it('should navigate to the default route when authentication succeeds', () => {
    authService.login.mockReturnValue(
      of({
        accessToken: 'jwt-token',
        tokenType: 'Bearer',
      }),
    );
    component.email.setValue('alice@test.com');
    component.password.setValue('password123');

    component.login();

    expect(router.navigate).toHaveBeenCalledOnce();
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should display a generic message when authentication fails', () => {
    authService.login.mockReturnValue(throwError(() => new Error('Authentication failed')));
    component.email.setValue('alice@test.com');
    component.password.setValue('wrong-password');

    component.login();
    fixture.detectChanges();

    expect(component.loginError()).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Email or password incorrect');
  });

  it('should clear the previous authentication error before a new attempt', () => {
    authService.login.mockReturnValue(NEVER);
    component.loginError.set(true);
    component.email.setValue('alice@test.com');
    component.password.setValue('password123');

    component.login();

    expect(component.loginError()).toBe(false);
    expect(authService.login).toHaveBeenCalledOnce();
  });

  it('should display a link to the registration page', () => {
    fixture.detectChanges();

    const registrationLink = fixture.nativeElement.querySelector('a[href="/register"]');
    expect(registrationLink).not.toBeNull();
    expect(registrationLink.textContent).toContain('Create an account');
  });
});
