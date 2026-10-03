import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { EMPTY, catchError, throwError } from 'rxjs';
import { AuthService } from './auth-service';

const PUBLIC_URLS = ['/api/auth/login', '/api/auth/register'];

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  if (!request.url.startsWith('/api/')) {
    return next(request);
  }

  const authService = inject(AuthService);
  const isPublicUrl = PUBLIC_URLS.includes(request.url);

  if (isPublicUrl) {
    return next(request);
  }

  const token = authService.getToken();
  const router = inject(Router);

  const authenticatedRequest = token
    ? request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      })
    : request;

  return next(authenticatedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        authService.logout();
        void router.navigate(['/login'], { replaceUrl: true });
        return EMPTY;
      }
      return throwError(() => error);
    }),
  );
};
