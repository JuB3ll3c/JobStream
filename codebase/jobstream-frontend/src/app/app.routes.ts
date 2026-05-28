import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'jobs',
    pathMatch: 'full'
  },
  {
    path: 'jobs',
    loadComponent: () => import('./features/job-search/job-search-page/job-search-page.component')
      .then(m => m.JobSearchPageComponent)
  },
  {
    path: 'jobs/:id',
    loadComponent: () => import('./features/job-search/job-details/job-details.component')
      .then(m => m.JobDetailsComponent)
  },
  {
    path: 'saved-jobs',
    loadComponent: () => import('./features/saved-jobs/saved-jobs-page/saved-jobs-page.component')
      .then(m => m.SavedJobsPageComponent)
  },
  {
    path: 'saved-jobs/:id/analysis',
    loadComponent: () => import('./features/saved-jobs/analysis-result/analysis-result.component')
      .then(m => m.AnalysisResultComponent)
  },
  // Fallback
  {
    path: '**',
    redirectTo: 'jobs'
  }
];
