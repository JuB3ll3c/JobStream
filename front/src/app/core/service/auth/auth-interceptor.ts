import { HttpInterceptorFn } from '@angular/common/http';

const PUBLIC_URLS = [
  '/api/auth/login',
  '/api/auth/register'
];

export const authInterceptor: HttpInterceptorFn = (request, next) => {

  const isPublicUrl = PUBLIC_URLS.some(url =>
    request.url.includes(url)
  );

  if (isPublicUrl) {
    return next(request);
  }

  const token = sessionStorage.getItem('access_token');
  const tokenType = sessionStorage.getItem('token_type');

  if (!token || !tokenType) {
    return next(request);
  }

  const authenticatedRequest = request.clone({
    setHeaders: {
      Authorization: `${tokenType} ${token}`
    }
  });

  return next(authenticatedRequest);
};
