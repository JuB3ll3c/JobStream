import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../../core/service/auth/auth-service';
import { RegisterRequest } from '../../../../generated';

function passwordsMatch(control: AbstractControl): ValidationErrors | null {
  return control.get('password')?.value === control.get('confirmPassword')?.value
    ? null
    : { passwordMismatch: true };
}

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  emailAlreadyExistsError = signal(false);
  registrationError = signal(false);

  firstName = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.pattern(/\S/)],
  });

  lastName = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.pattern(/\S/)],
  });

  email = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.email],
  });

  password = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.minLength(8)],
  });

  confirmPassword = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required],
  });

  registerForm = new FormGroup(
    {
      firstName: this.firstName,
      lastName: this.lastName,
      email: this.email,
      password: this.password,
      confirmPassword: this.confirmPassword,
    },
    { validators: passwordsMatch },
  );

  register(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.emailAlreadyExistsError.set(false);
    this.registrationError.set(false);

    const registerRequest: RegisterRequest = {
      firstName: this.firstName.value,
      lastName: this.lastName.value,
      email: this.email.value,
      password: this.password.value,
    };

    this.authService.register(registerRequest).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 409) {
          this.emailAlreadyExistsError.set(true);
        } else {
          this.registrationError.set(true);
        }
      },
    });
  }
}
