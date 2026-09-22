import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/service/auth/auth-service';
import { LoginRequest } from '../../../generated';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  loginError = signal(false);

  email = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.email],
  });

  password = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required],
  });

  loginForm = new FormGroup({
    email: this.email,
    password: this.password,
  });

  login(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loginError.set(false);

    const loginRequest: LoginRequest = {
      email: this.email.value,
      password: this.password.value,
    };

    this.authService.login(loginRequest).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: () => {
        this.loginError.set(true);
      },
    });
  }
}
