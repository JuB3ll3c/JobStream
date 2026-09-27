import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Register } from './register';

describe('Register', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Register],
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
});
