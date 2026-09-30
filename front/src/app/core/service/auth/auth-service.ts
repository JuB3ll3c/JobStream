import { inject, Injectable } from '@angular/core';
import { tap } from 'rxjs';
import {
  AuthenticationService,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  RoleDto,
} from '../../../generated';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly authenticationApi = inject(AuthenticationService);

  private readonly tokenKey = 'access_token';
  private readonly tokenTypeKey = 'token_type';
  private readonly roleKey = 'role';

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
    sessionStorage.setItem(this.roleKey, response.role);
  }

  logout(): void {
    sessionStorage.removeItem(this.tokenKey);
    sessionStorage.removeItem(this.tokenTypeKey);
    sessionStorage.removeItem(this.roleKey);
  }

  getToken(): string | null {
    return sessionStorage.getItem(this.tokenKey);
  }

  getTokenType(): string | null {
    return sessionStorage.getItem(this.tokenTypeKey);
  }

  getRole(): RoleDto | null {
    const role = sessionStorage.getItem(this.roleKey);
    return Object.values(RoleDto).find((knownRole) => knownRole === role) ?? null;
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }
}
