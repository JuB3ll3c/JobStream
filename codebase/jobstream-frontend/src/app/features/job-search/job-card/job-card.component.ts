import { Component, input, signal, inject, effect, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { JobDto } from '../../../core/models';
import { JobService } from '../../../core/services/job.service';
import { SalaryFormatter } from '../../../shared/utils/salary-formatter.util';
import { DateFormatter } from '../../../shared/utils/date-formatter.util';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-job-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './job-card.component.html',
  styleUrl: './job-card.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class JobCardComponent {
  private readonly router = inject(Router);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly jobService = inject(JobService);

  job = input.required<JobDto>();
  isSaved = input.required<boolean>();
  isSaving = signal(false);
  
  // Local signal for saved state - synced with input
  private readonly _isSaved = signal(false);
  
  constructor() {
    // Sync input to local signal
    effect(() => {
      this._isSaved.set(this.isSaved());
    });
  }
  
  // Getter for template
  get isSavedValue() {
    return this._isSaved();
  }

  // Helper to extract value from JsonNullable objects (backend sends {present: true/false})
  extractValue(value: any): any {
    if (!value) return undefined;
    if (typeof value === 'object' && 'present' in value) {
      return value.present ? value.value : undefined;
    }
    return value;
  }

  // Alias for template use
  extractValueForTemplate(value: any): any {
    return this.extractValue(value);
  }

  onSaveJob(event: Event): void {
    event.stopPropagation();
    
    const job = this.job();
    
    if (this._isSaved()) {
      // Already saved - could implement unsave functionality here
      return;
    }

    if (this.isSaving()) {
      return;
    }

    this.isSaving.set(true);

    // Extract values from JsonNullable objects
    const description = this.extractValue(job.description);
    const contractType = this.extractValue(job.contractType);
    const postedDate = this.extractValue(job.postedDate);
    const requirements = this.extractValue(job.requirements);
    const benefits = this.extractValue(job.benefits);
    const jobUrl = this.extractValue(job.jobUrl);

    // Build request - only include optional fields if they have values
    const request: any = {
      externalId: job.externalId,
      title: job.title,
      company: job.company,
      location: job.location || ''
    };

    if (description) request.description = description;
    if (contractType) request.contractType = contractType;
    if (postedDate) request.postedDate = postedDate;
    if (jobUrl) request.jobUrl = jobUrl;
    if (requirements && Array.isArray(requirements) && requirements.length > 0) request.requirements = requirements;
    if (benefits && Array.isArray(benefits) && benefits.length > 0) request.benefits = benefits;

    this.jobService.saveJob(request).subscribe({
      next: () => {
        this.isSaving.set(false);
        this._isSaved.set(true);
      },
      error: () => {
        this.isSaving.set(false);
      }
    });
  }

  onApply(event: Event): void {
    event.stopPropagation();
    const jobUrl = this.job().jobUrl;
    if (jobUrl) {
      const safeUrl = this.getSafeUrl(jobUrl);
      if (safeUrl) {
        window.open(safeUrl, '_blank', 'noopener,noreferrer');
      }
    }
  }

  viewJobDetails(): void {
    this.router.navigate(['/jobs', this.job().externalId]);
  }

  formatSalary(): string {
    const job = this.job();
    // Extract values from JsonNullable objects
    const salaryMin = this.extractValue(job.salaryMin);
    const salaryMax = this.extractValue(job.salaryMax);
    return SalaryFormatter.formatSalaryRange(salaryMin, salaryMax);
  }

  getTimeAgo(): string {
    // Use postedDate from the job if available and valid
    const postedDate = this.extractValue(this.job().postedDate);
    if (postedDate && typeof postedDate === 'string' && postedDate.trim()) {
      return DateFormatter.getTimeAgo(postedDate);
    }
    // Fallback to a default date (for jobs without postedDate)
    const defaultDate = new Date(Date.now() - 2 * 60 * 60 * 1000);
    return DateFormatter.getTimeAgo(defaultDate);
  }

  private getSafeUrl(url: string): string | null {
    try {
      const parsedUrl = new URL(url);
      
      // Verify it's a valid HTTP/HTTPS URL
      if (!['http:', 'https:'].includes(parsedUrl.protocol)) {
        console.warn('Invalid URL protocol:', parsedUrl.protocol);
        return null;
      }

      // Sanitize the URL to prevent XSS
      const safeUrl: SafeResourceUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
      
      return safeUrl.toString();
    } catch (error) {
      console.error('Invalid URL:', url, error);
      return null;
    }
  }
}
