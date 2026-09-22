import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';
import { AuthResponse, LoginRequest } from '../../../generated';


@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = '/api/auth';

  private readonly tokenKey = 'access_token';
  private readonly tokenTypeKey = 'token_type';

  login(request: LoginRequest) {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap(response => {
          sessionStorage.setItem(this.tokenKey, response.accessToken);
          sessionStorage.setItem(this.tokenTypeKey, response.tokenType);
        })
      );
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
