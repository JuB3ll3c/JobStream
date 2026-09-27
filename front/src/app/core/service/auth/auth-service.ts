import { inject, Injectable } from '@angular/core';
import { tap } from 'rxjs';
import {
  AuthenticationService,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
} from '../../../generated';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly authenticationApi = inject(AuthenticationService);

  private readonly tokenKey = 'access_token';
  private readonly tokenTypeKey = 'token_type';

  login(request: LoginRequest) {
    return this.authenticationApi
      .login({ loginRequest: request })
      .pipe(tap((response) => this.storeAuthentication(response)));
  }

  register(request: RegisterRequest) {
    return this.authenticationApi
      .register({ registerRequest: request })
      .pipe(tap((response) => this.storeAuthentication(response)));
  }

  private storeAuthentication(response: AuthResponse): void {
    sessionStorage.setItem(this.tokenKey, response.accessToken);
    sessionStorage.setItem(this.tokenTypeKey, response.tokenType);
  }

  logout(): void {
    sessionStorage.removeItem(this.tokenKey);
    sessionStorage.removeItem(this.tokenTypeKey);
  }

  getToken(): string | null {
    return sessionStorage.getItem(this.tokenKey);
  }

  getTokenType(): string | null {
    return sessionStorage.getItem(this.tokenTypeKey);
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }
}
