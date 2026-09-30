import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';

import { RoleDto } from '../../../generated';
import { AuthService } from './auth-service';
import { roleGuard } from './role-guard';

describe('roleGuard', () => {
  const executeGuard = (requiredRole: RoleDto) => {
    const guard: CanActivateFn = roleGuard(requiredRole);
    return TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );
  };

  let authService: { getRole: ReturnType<typeof vi.fn> };
  let router: { createUrlTree: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    authService = { getRole: vi.fn() };
    router = { createUrlTree: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('should allow a user with the required role to activate the route', () => {
    authService.getRole.mockReturnValue(RoleDto.Admin);

    const result = executeGuard(RoleDto.Admin);

    expect(result).toBe(true);
    expect(router.createUrlTree).not.toHaveBeenCalled();
  });

  it('should redirect a user with a different role to the jobs page', () => {
    const jobsUrlTree = {} as UrlTree;
    authService.getRole.mockReturnValue(RoleDto.User);
    router.createUrlTree.mockReturnValue(jobsUrlTree);

    const result = executeGuard(RoleDto.Admin);

    expect(result).toBe(jobsUrlTree);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/jobs']);
  });

  it('should redirect a user without a role to the jobs page', () => {
    const jobsUrlTree = {} as UrlTree;
    authService.getRole.mockReturnValue(null);
    router.createUrlTree.mockReturnValue(jobsUrlTree);

    const result = executeGuard(RoleDto.Admin);

    expect(result).toBe(jobsUrlTree);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/jobs']);
  });
});
