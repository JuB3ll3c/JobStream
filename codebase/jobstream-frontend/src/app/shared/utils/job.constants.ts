/**
 * Constants for job-search module
 */

export const SALARY_CONVERSION_FACTOR = 1000;
export const DEFAULT_PAGE_SIZE = 20;
export const DEFAULT_SALARY_RANGE: [number, number] = [40, 180];
export const SALARY_UNIT = 'k';

export const JOB_TYPES = ['Full-time', 'Part-time', 'Contract', 'Internship'] as const;

export const EXPERIENCE_LEVELS = ['Junior', 'Mid-Level', 'Senior', 'Lead'] as const;

export const POPULAR_SEARCHES = ['UI Design', 'Backend Engineer', 'Product Manager'] as const;

export const SALARY_FORMAT_OPTIONS = {
  MINIMUM_SALARY: 0,
  MAXIMUM_SALARY: 500,
  SALARY_STEP: 10
} as const;
