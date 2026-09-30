import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { RoleDto } from '../../../generated';
import { AuthService } from './auth-service';

export const roleGuard =
  (requiredRole: RoleDto): CanActivateFn =>
  () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.getRole() === requiredRole) {
      return true;
    }

    return router.createUrlTree(['/jobs']);
  };
