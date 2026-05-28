import { ComponentFixture, TestBed } from '@angular/core/testing';
import { JobCardComponent } from './job-card.component';
import { JobDto } from '../../../core/models';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { vi } from 'vitest';

describe('JobCardComponent', () => {
  let component: JobCardComponent;
  let fixture: ComponentFixture<JobCardComponent>;
  let router: Router;

  const mockJob: JobDto = {
    externalId: 'test-id',
    title: 'Software Engineer',
    company: 'Tech Corp',
    location: 'San Francisco',
    description: 'Develop amazing software',
    salaryMin: 80000,
    salaryMax: 120000,
    contractType: 'Full-time',
    jobUrl: 'https://example.com/job',
    requirements: ['JavaScript', 'Angular'],
    benefits: ['Health insurance', 'Remote work']
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JobCardComponent],
      providers: [
        provideRouter([]),
        provideHttpClient()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(JobCardComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should format salary correctly', () => {
    // Access the input signal function to get the value
    const jobValue = component.job();
    expect(component.formatSalary()).toBe('Salary not specified');
  });

  it('should toggle saved state', () => {
    expect(component.isSaved()).toBe(false);
    
    const event = new Event('click');
    component.onSaveJob(event);
    
    expect(component.isSaved()).toBe(true);
    
    component.onSaveJob(event);
    expect(component.isSaved()).toBe(false);
  });

  it('should navigate to job details', () => {
    const navigateSpy = vi.spyOn(router, 'navigate');
    component.viewJobDetails();
    
    expect(navigateSpy).toHaveBeenCalled();
  });

  it('should get time ago', () => {
    expect(component.getTimeAgo()).toBeTruthy();
  });
});
