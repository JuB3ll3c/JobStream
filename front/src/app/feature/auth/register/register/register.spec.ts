import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { NEVER, of } from 'rxjs';

import { AuthService } from '../../../../core/service/auth/auth-service';
import { Register } from './register';

describe('Register', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;
  let authService: { register: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    authService = { register: vi.fn() };
    router = { navigate: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [Register],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should touch all fields when the form is invalid', () => {
    component.register();

    expect(authService.register).not.toHaveBeenCalled();
    expect(component.firstName.touched).toBe(true);
    expect(component.lastName.touched).toBe(true);
    expect(component.email.touched).toBe(true);
    expect(component.password.touched).toBe(true);
    expect(component.confirmPassword.touched).toBe(true);
  });

  it('should reject blank first and last names', () => {
    component.firstName.setValue('   ');
    component.lastName.setValue('\t');

    expect(component.firstName.invalid).toBe(true);
    expect(component.lastName.invalid).toBe(true);
  });

  it('should reject an invalid email address', () => {
    component.email.setValue('alice');

    expect(component.email.hasError('email')).toBe(true);
  });

  it('should reject a password shorter than eight characters', () => {
    component.password.setValue('short');

    expect(component.password.hasError('minlength')).toBe(true);
  });

  it('should reject a password confirmation that does not match', () => {
    component.password.setValue('password123');
    component.confirmPassword.setValue('different123');

    expect(component.registerForm.hasError('passwordMismatch')).toBe(true);
  });

  it('should display validation messages after an invalid submission', () => {
    component.register();
    fixture.detectChanges();

    const content = fixture.nativeElement.textContent;
    expect(content).toContain('First name required');
    expect(content).toContain('Last name required');
    expect(content).toContain('Email required');
    expect(content).toContain('Password required');
    expect(content).toContain('Password confirmation required');
  });

  it('should submit the registration data when the form is valid', () => {
    authService.register.mockReturnValue(NEVER);
    component.firstName.setValue('Alice');
    component.lastName.setValue('Martin');
    component.email.setValue('alice@test.com');
    component.password.setValue('password123');
    component.confirmPassword.setValue('password123');

    component.register();

    expect(authService.register).toHaveBeenCalledOnce();
    expect(authService.register).toHaveBeenCalledWith({
      firstName: 'Alice',
      lastName: 'Martin',
      email: 'alice@test.com',
      password: 'password123',
    });
  });

  it('should navigate to the default route when registration succeeds', () => {
    authService.register.mockReturnValue(
      of({
        accessToken: 'jwt-token',
        tokenType: 'Bearer',
      }),
    );
    component.firstName.setValue('Alice');
    component.lastName.setValue('Martin');
    component.email.setValue('alice@test.com');
    component.password.setValue('password123');
    component.confirmPassword.setValue('password123');

    component.register();

    expect(router.navigate).toHaveBeenCalledOnce();
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });
});
