import { authGuard } from './core/service/auth/auth-guard';
import { Jobs } from './feature/jobs/jobs/jobs';
import { routes } from './app.routes';

describe('application routes', () => {
  it('should use the protected jobs page as the default destination', async () => {
    const rootRoute = routes.find((route) => route.path === '');
    const loginRoute = routes.find((route) => route.path === 'login');
    const jobsRoute = routes.find((route) => route.path === 'jobs');

    expect(rootRoute).toEqual({
      path: '',
      redirectTo: 'jobs',
      pathMatch: 'full',
    });
    expect(loginRoute?.canActivate).toBeUndefined();
    expect(jobsRoute?.canActivate).toEqual([authGuard]);
    await expect(jobsRoute?.loadComponent?.()).resolves.toBe(Jobs);
  });
});
