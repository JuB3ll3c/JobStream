import { Component, signal, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { JobService } from '../../../core/services/job.service';
import { JobDto, SaveJobRequest, SavedJobDto, AnalysisDto } from '../../../core/models';
import { LoggerService } from '../../../core/services/logger.service';
import { ErrorHandlerService } from '../../../core/services/error-handler.service';
import { DateFormatter } from '../../../shared/utils/date-formatter.util';
import { SalaryFormatter } from '../../../shared/utils/salary-formatter.util';

interface JobAnalysis {
  score: number | null;
  summary: string | null;
  strengths: string[] | null;
  weaknesses: string[] | null;
  recommendations: string[] | null;
  tags: string[] | null;
}

@Component({
  selector: 'app-job-details',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './job-details.component.html',
  styleUrl: './job-details.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class JobDetailsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly jobService = inject(JobService);
  private readonly logger = inject(LoggerService);
  private readonly errorHandler = inject(ErrorHandlerService);

  protected readonly job = signal<JobDto | null>(null);
  protected readonly isLoading = signal<boolean>(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly isSaving = signal<boolean>(false);
  protected readonly isSaved = signal<boolean>(false);
  protected readonly analysis = signal<JobAnalysis | null>(null);

  ngOnInit(): void {
    const externalId = this.route.snapshot.paramMap.get('id');

    if (!externalId) {
      this.errorMessage.set('Job ID not found');
      this.isLoading.set(false);
      return;
    }

    this.loadJob(externalId);
  }

  private loadJob(externalId: string): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.jobService.getJobById(externalId).subscribe({
      next: (job: JobDto) => {
        this.job.set(job);
        this.isLoading.set(false);

        // Check if job is saved and has analysis
        this.checkSavedJobAnalysis(externalId);

        this.logger.info('Job details loaded', { job });
      },
      error: (error: Error) => {
        const errorMessage = this.errorHandler.handleGenericError(error);
        this.errorMessage.set(errorMessage);
        this.isLoading.set(false);

        this.logger.error('Failed to load job details', {
          externalId,
          error: error.message
        });
      }
    });
  }

  private checkSavedJobAnalysis(externalId: string): void {
    this.jobService.checkSavedJobs([externalId]).subscribe({
      next: (savedIds) => {
        if (savedIds.length > 0 && savedIds[0]) {
          // Job is saved, get analysis using internal UUID
          this.loadSavedJobAnalysis(savedIds[0]);
        }
      },
      error: () => {
        // Ignore errors, just won't show analysis
      }
    });
  }

  private loadSavedJobAnalysis(savedJobId: string): void {
    this.jobService.getAnalysisBySavedJobId(savedJobId).subscribe({
      next: (data: any) => {
        if (data && data.score != null) {
          this.analysis.set({
            score: data.score,
            summary: data.summary,
            strengths: data.strengths,
            weaknesses: data.weaknesses,
            recommendations: data.recommendations,
            tags: data.tags
          });
        }
      },
      error: () => {
        // Ignore errors, just won't show analysis
      }
    });
  }

  protected getScoreColor(score: number | null): string {
    if (score === null) return '#ccc';
    if (score >= 80) return '#22c55e';
    if (score >= 60) return '#f59e0b';
    return '#ef4444';
  }

  protected getScoreLabel(score: number | null): string {
    if (score === null) return 'N/A';
    if (score >= 80) return 'Excellent Match';
    if (score >= 60) return 'Good Match';
    if (score >= 40) return 'Fair Match';
    return 'Low Match';
  }

  protected formatDate(date: string | undefined | null): string {
    return DateFormatter.formatLocalDate(date ?? undefined);
  }

  protected formatSalary(min: number | undefined, max: number | undefined): string {
    return SalaryFormatter.formatSalaryRange(min ?? null, max ?? null);
  }

  protected applyToJob(): void {
    const jobUrl = this.job()?.jobUrl;
    if (jobUrl) {
      window.open(jobUrl, '_blank');
      this.logger.info('User applied to job', {
        externalId: this.job()?.externalId,
        url: jobUrl
      });
    }
  }

  protected saveJob(): void {
    const job = this.job();
    if (!job || this.isSaving() || this.isSaved()) {
      return;
    }

    this.isSaving.set(true);
    this.successMessage.set(null);
    this.errorMessage.set(null);

    // Helper to extract value from JsonNullable objects (backend sends {present: true/false})
    // or return the value directly if it's a plain string/number
    const extractValue = (value: any): any => {
      if (!value) return undefined;
      // Check if it's a JsonNullable object with 'present' property
      if (typeof value === 'object' && 'present' in value) {
        return value.present ? value.value : undefined;
      }
      // Return as-is for normal values
      return value;
    };

    // Extract values from job object
    const description = extractValue(job.description);
    const contractType = extractValue(job.contractType);
    const postedDate = extractValue(job.postedDate);
    const requirements = extractValue(job.requirements);
    const benefits = extractValue(job.benefits);
    const jobUrl = extractValue(job.jobUrl);

    // Build request - only include optional fields if they have values
    const request: any = {
      externalId: job.externalId,
      title: job.title,
      company: job.company,
      location: job.location || ''
    };

    // Only add optional fields if they have values
    if (description) request.description = description;
    if (contractType) request.contractType = contractType;
    if (postedDate) request.postedDate = postedDate;
    if (jobUrl) request.jobUrl = jobUrl;
    if (requirements && Array.isArray(requirements) && requirements.length > 0) request.requirements = requirements;
    if (benefits && Array.isArray(benefits) && benefits.length > 0) request.benefits = benefits;

    this.jobService.saveJob(request).subscribe({
      next: (savedJob) => {
        this.isSaving.set(false);
        this.isSaved.set(true);
        this.successMessage.set('Offre sauvegardée');

        this.logger.info('Job saved successfully', {
          externalId: job.externalId,
          savedJobId: savedJob.id
        });
      },
      error: (error: Error) => {
        this.isSaving.set(false);
        const errorMsg = this.errorHandler.handleGenericError(error);
        this.errorMessage.set(errorMsg);

        this.logger.error('Failed to save job', {
          externalId: job.externalId,
          error: error.message
        });
      }
    });
  }

  protected goBack(): void {
    // Return to search results or home
    const previousUrl = this.route.snapshot.queryParams['from'];
    if (previousUrl === 'search') {
      this.router.navigate(['/jobs']);
    } else {
      this.router.navigate(['/jobs']);
    }
  }
}
