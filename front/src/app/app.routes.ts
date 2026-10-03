import { Routes } from '@angular/router';

import { authGuard } from './core/service/auth/auth-guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'jobs',
    pathMatch: 'full',
  },
  {
    path: 'login',
    loadComponent: () => import('./feature/auth/login/login').then((m) => m.Login),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./feature/auth/register/register/register').then((m) => m.Register),
  },
  {
    path: 'jobs',
    canActivate: [authGuard],
    loadComponent: () => import('./feature/jobs/jobs/jobs').then((m) => m.Jobs),
  },
  {
    path: 'jobs/:externalId',
    canActivate: [authGuard],
    loadComponent: () => import('./feature/jobs/job-detail/job-detail').then((m) => m.JobDetail),
  },
];
