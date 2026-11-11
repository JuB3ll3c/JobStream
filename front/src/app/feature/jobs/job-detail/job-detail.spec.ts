import { TestBed } from '@angular/core/testing';
import { JobDetail } from './job-detail';

describe('JobDetail presentation', () => {
  it('renders the supplied job without routing or HTTP providers', async () => {
    await TestBed.configureTestingModule({ imports: [JobDetail] }).compileComponents();
    const fixture = TestBed.createComponent(JobDetail);
    fixture.componentRef.setInput('job', {
      externalId: 'external',
      title: 'Java Engineer',
      company: 'Acme',
      location: 'Zurich',
      description: 'Build useful software',
      salaryMin: 80000,
      salaryMax: 100000,
      postedDate: '2026-11-01',
      contractType: 'Permanent',
      requirements: ['Java', 'Spring'],
      jobUrl: 'https://example.com/job',
    });
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent;
    for (const value of [
      'Java Engineer',
      'Acme',
      'Zurich',
      'Build useful software',
      '80000',
      '100000',
      '01/11/2026',
      'Permanent',
      'Spring',
    ])
      expect(text).toContain(value);
    expect(fixture.nativeElement.querySelector('a').href).toBe('https://example.com/job');
    expect(fixture.nativeElement.querySelector('button')).toBeNull();
    expect(fixture.nativeElement.querySelector('select')).toBeNull();
  });

  it('updates its display when the input changes and handles missing optional data', async () => {
    await TestBed.configureTestingModule({ imports: [JobDetail] }).compileComponents();
    const fixture = TestBed.createComponent(JobDetail);
    fixture.componentRef.setInput('job', {
      externalId: 'one',
      title: 'First',
      company: 'Acme',
      location: 'Zurich',
    });
    fixture.detectChanges();
    fixture.componentRef.setInput('job', {
      externalId: 'two',
      title: 'Second',
      company: 'Other',
      location: 'Bern',
    });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Second');
    expect(fixture.nativeElement.textContent).not.toContain('First');
    expect(fixture.nativeElement.textContent).toContain('Aucune description disponible');
  });
});
