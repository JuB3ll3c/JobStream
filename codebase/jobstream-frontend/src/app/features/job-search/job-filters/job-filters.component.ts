import { Component, output, input, ChangeDetectionStrategy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import {
  JOB_TYPES,
  EXPERIENCE_LEVELS,
  DEFAULT_SALARY_RANGE,
  SALARY_FORMAT_OPTIONS
} from '../../../shared/utils/job.constants';
import { SalaryFormatter } from '../../../shared/utils/salary-formatter.util';

export interface FilterChangeEvent {
  jobType: string | null;
  experienceLevel: string | null;
  salaryRange: [number, number];
}

@Component({
  selector: 'app-job-filters',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './job-filters.component.html',
  styleUrl: './job-filters.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class JobFiltersComponent implements OnInit {
  // Inputs
  selectedJobType = input<string | null>(null);
  selectedExperienceLevel = input<string | null>(null);
  salaryRange = input<[number, number]>(DEFAULT_SALARY_RANGE);

  // Outputs
  filtersChanged = output<FilterChangeEvent>();
  filtersCleared = output<void>();

  // Form
  protected filterForm!: FormGroup;

  // Constants
  protected readonly jobTypes = JOB_TYPES;
  protected readonly experienceLevels = EXPERIENCE_LEVELS;
  protected readonly salaryOptions = SALARY_FORMAT_OPTIONS;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    const [minSalary, maxSalary] = this.salaryRange();
    
    this.filterForm = this.fb.group({
      jobType: [this.selectedJobType()],
      experienceLevel: [this.selectedExperienceLevel()],
      salaryMin: [minSalary, [Validators.min(0), Validators.max(this.salaryOptions.MAXIMUM_SALARY)]],
      salaryMax: [maxSalary, [Validators.min(0), Validators.max(this.salaryOptions.MAXIMUM_SALARY)]]
    }, { validators: this.salaryRangeValidator });
  }

  /**
   * Custom validator to ensure min salary <= max salary
   */
  private salaryRangeValidator(form: FormGroup) {
    const minSalary = form.get('salaryMin')?.value;
    const maxSalary = form.get('salaryMax')?.value;

    if (minSalary !== null && maxSalary !== null && minSalary > maxSalary) {
      return { invalidSalaryRange: true };
    }
    return null;
  }

  onJobTypeSelect(type: string): void {
    const currentValue = this.filterForm.get('jobType')?.value;
    const newValue = currentValue === type ? null : type;
    this.filterForm.patchValue({ jobType: newValue });
    this.emitFilters();
  }

  onExperienceLevelSelect(level: string): void {
    const currentValue = this.filterForm.get('experienceLevel')?.value;
    const newValue = currentValue === level ? null : level;
    this.filterForm.patchValue({ experienceLevel: newValue });
    this.emitFilters();
  }

  onSalaryMinChange(): void {
    if (this.filterForm.valid) {
      this.emitFilters();
    }
  }

  onSalaryMaxChange(): void {
    if (this.filterForm.valid) {
      this.emitFilters();
    }
  }

  clearFilters(): void {
    this.filterForm.reset({
      jobType: null,
      experienceLevel: null,
      salaryMin: DEFAULT_SALARY_RANGE[0],
      salaryMax: DEFAULT_SALARY_RANGE[1]
    });
    this.filtersCleared.emit();
  }

  formatSalaryValue(value: number): string {
    return SalaryFormatter.formatToK(value);
  }

  get salaryRangeError(): boolean {
    return this.filterForm.hasError('invalidSalaryRange');
  }

  private emitFilters(): void {
    const { jobType, experienceLevel, salaryMin, salaryMax } = this.filterForm.value;
    
    this.filtersChanged.emit({
      jobType: jobType || null,
      experienceLevel: experienceLevel || null,
      salaryRange: [salaryMin || 0, salaryMax || 999999]
    });
  }
}
