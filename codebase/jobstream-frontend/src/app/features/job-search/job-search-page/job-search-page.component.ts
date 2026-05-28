import { Component, signal, inject, OnInit, computed, ChangeDetectionStrategy, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { JobService } from '../../../core/services/job.service';
import { JobCacheService } from '../../../core/services/cache.service';
import { JobCardComponent } from '../job-card/job-card.component';
import { SearchBarComponent } from '../search-bar/search-bar.component';
import { JobFiltersComponent } from '../job-filters/job-filters.component';
import { JobDto, JobSearchResponse, SavedJobDto } from '../../../core/models';
import { LoggerService } from '../../../core/services/logger.service';
import { ErrorHandlerService } from '../../../core/services/error-handler.service';
import { DEFAULT_PAGE_SIZE, DEFAULT_SALARY_RANGE } from '../../../shared/utils/job.constants';

@Component({
  selector: 'app-job-search-page',
  standalone: true,
  imports: [
    CommonModule,
    JobCardComponent,
    SearchBarComponent,
    JobFiltersComponent,
    RouterModule
  ],
  templateUrl: './job-search-page.component.html',
  styleUrl: './job-search-page.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class JobSearchPageComponent implements OnInit {
  private readonly jobService = inject(JobService);
  private readonly jobCache = inject(JobCacheService);
  private readonly logger = inject(LoggerService);
  private readonly errorHandler = inject(ErrorHandlerService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  // Search state
  protected readonly searchQuery = signal<string>('');
  protected readonly locationQuery = signal<string>('');
  protected readonly selectedJobType = signal<string | null>(null);
  protected readonly selectedExperienceLevel = signal<string | null>(null);
  protected readonly salaryRange = signal<[number, number]>(DEFAULT_SALARY_RANGE);

  // Results state
  protected readonly jobs = signal<JobDto[]>([]);
  protected readonly totalCount = signal<number>(0);
  protected readonly isLoading = signal<boolean>(false);
  protected readonly errorMessage = signal<string | null>(null);
  
  // Saved jobs tracking - Map of externalId to boolean
  protected readonly savedJobsMap = signal<Map<string, boolean>>(new Map());
  
  // Helper to check if a job is saved
  protected isJobSaved(externalId: string): boolean {
    return this.savedJobsMap().get(externalId) === true;
  }

  // Pagination
  protected readonly currentPage = signal<number>(1);
  protected readonly pageSize = DEFAULT_PAGE_SIZE;
  protected readonly hasMoreJobs = computed(() =>
    this.jobs().length < this.totalCount()
  );

  // Computed pagination
  protected readonly totalPages = computed(() =>
    Math.ceil(this.totalCount() / this.pageSize)
  );

  protected readonly showPagination = computed(() =>
    this.totalPages() > 1
  );

  protected readonly pageNumbers = computed(() => {
    const pages: number[] = [];
    const total = this.totalPages();
    const current = this.currentPage();

    // Simple pagination: show all pages if <= 7, otherwise show first, last, and around current
    if (total <= 7) {
      for (let i = 1; i <= total; i++) {
        pages.push(i);
      }
    } else {
      pages.push(1);
      if (current > 3) pages.push(-1); // ellipsis
      for (let i = Math.max(2, current - 1); i <= Math.min(total - 1, current + 1); i++) {
        pages.push(i);
      }
      if (current < total - 2) pages.push(-1); // ellipsis
      pages.push(total);
    }

    return pages;
  });

  ngOnInit(): void {
    // Read initial state from URL
    this.route.queryParams.subscribe(params => {
      const q = params['q'];
      const page = params['page'];
      const location = params['location'];
      
      if (q) {
        this.searchQuery.set(q);
      }
      if (location) {
        this.locationQuery.set(location);
      }
      if (page) {
        this.currentPage.set(parseInt(page, 10) || 1);
      }
      
      if (this.searchQuery().trim()) {
        this.performSearch();
      }
    });
  }

  protected onSearchSubmitted(event: { query: string; location: string }): void {
    this.searchQuery.set(event.query);
    this.locationQuery.set(event.location);
    this.currentPage.set(1);
    this.updateUrl();
    this.performSearch();
  }

  protected onPopularSearchClicked(term: string): void {
    this.searchQuery.set(term);
    this.locationQuery.set('');
    this.currentPage.set(1);
    this.updateUrl();
    this.performSearch();
  }

  protected onFiltersChanged(event: {
    jobType: string | null;
    experienceLevel: string | null;
    salaryRange: [number, number]
  }): void {
    this.selectedJobType.set(event.jobType);
    this.selectedExperienceLevel.set(event.experienceLevel);
    this.salaryRange.set(event.salaryRange);
    this.currentPage.set(1);
    this.updateUrl();
    this.performSearch();
  }

  protected onFiltersCleared(): void {
    this.selectedJobType.set(null);
    this.selectedExperienceLevel.set(null);
    this.salaryRange.set(DEFAULT_SALARY_RANGE);
    this.currentPage.set(1);
    this.updateUrl();
    this.performSearch();
  }

  protected loadMore(): void {
    const nextPage = this.currentPage() + 1;
    this.currentPage.set(nextPage);
    this.updateUrl();
    this.performSearch(true);
  }

  protected goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages() && page !== this.currentPage()) {
      this.currentPage.set(page);
      this.updateUrl();
      this.performSearch();
    }
  }

  protected previousPage(): void {
    if (this.currentPage() > 1) {
      this.currentPage.update(p => p - 1);
      this.updateUrl();
      this.performSearch();
    }
  }

  protected nextPage(): void {
    if (this.currentPage() < this.totalPages()) {
      this.currentPage.update(p => p + 1);
      this.updateUrl();
      this.performSearch();
    }
  }

  protected newSearch(): void {
    this.searchQuery.set('');
    this.locationQuery.set('');
    this.currentPage.set(1);
    this.jobs.set([]);
    this.totalCount.set(0);
    this.router.navigate(['/jobs']);
  }

  private updateUrl(): void {
    const queryParams: Record<string, string> = {};
    
    if (this.searchQuery()) {
      queryParams['q'] = this.searchQuery();
    }
    if (this.locationQuery()) {
      queryParams['location'] = this.locationQuery();
    }
    if (this.currentPage() > 1) {
      queryParams['page'] = this.currentPage().toString();
    }
    
    this.router.navigate(['/jobs'], { 
      queryParams,
      replaceUrl: true 
    });
  }

  private performSearch(appendResults: boolean = false): void {
    const query = this.searchQuery().trim();
    if (!query) {
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.jobService.searchJobs(
      query,
      this.currentPage(),
      this.pageSize
    ).subscribe({
      next: (response: JobSearchResponse) => {
        this.handleSearchSuccess(response, appendResults);
        // Also fetch saved jobs to know which ones are already saved
        this.loadSavedJobs();
      },
      error: (error: Error) => {
        this.handleSearchError(error);
      }
    });
  }

  private loadSavedJobs(): void {
    const jobsList = this.jobs();
    if (jobsList.length === 0) {
      return;
    }
    
    // Extract externalIds from current jobs
    const externalIds = jobsList.map(job => job.externalId);
    
    this.jobService.checkSavedJobs(externalIds).subscribe({
      next: (savedIds: string[]) => {
        // Create a Map of externalId -> true for saved jobs
        const savedMap = new Map<string, boolean>();
        savedIds.forEach(id => {
          savedMap.set(id, true);
        });
        this.savedJobsMap.set(savedMap);
      },
      error: () => {
        // Silently fail - saved jobs are not critical
        this.savedJobsMap.set(new Map());
      }
    });
  }

  private handleSearchSuccess(response: JobSearchResponse, appendResults: boolean): void {
    if (appendResults) {
      this.jobs.update(currentJobs => [...currentJobs, ...(response.jobs || [])]);
    } else {
      this.jobs.set(response.jobs || []);
    }

    this.totalCount.set(response.total || 0);
    this.isLoading.set(false);

    this.logger.info('Job search successful', {
      query: this.searchQuery(),
      page: this.currentPage(),
      results: response.jobs?.length || 0,
      total: response.total || 0
    });
  }

  private handleSearchError(error: Error): void {
    const errorMessage = this.errorHandler.handleGenericError(error);
    this.errorMessage.set(errorMessage);
    this.isLoading.set(false);

    this.logger.error('Job search failed', {
      query: this.searchQuery(),
      page: this.currentPage(),
      error: error.message
    });
  }
}
