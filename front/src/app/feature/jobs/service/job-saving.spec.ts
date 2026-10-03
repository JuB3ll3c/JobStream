import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { BASE_PATH, JobDto } from '../../../generated';
import { JobSaving } from './job-saving';

describe('JobSaving', () => {
  let saving: JobSaving;
  let http: HttpTestingController;
  const offer: JobDto = {
    externalId: 'ext-1',
    title: 'Java Engineer',
    company: 'Acme',
    location: 'Zurich',
    description: 'An excerpt',
    salaryMin: 80000,
    salaryMax: 100000,
    contractType: 'permanent',
    postedDate: '2026-10-01',
    jobUrl: 'https://example.com/job/1',
    requirements: ['Java'],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        JobSaving,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: BASE_PATH, useValue: '/api' },
      ],
    });
    saving = TestBed.inject(JobSaving);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    try {
      http.verify();
    } finally {
      TestBed.resetTestingModule();
    }
  });

  it('allows retrying an offer after a technical error', () => {
    saving.save(offer);
    http.expectOne('/api/jobs').flush({}, { status: 500, statusText: 'Error' });
    expect(saving.states().get('ext-1')).toBe('error');
    saving.save(offer);
    expect(saving.states().get('ext-1')).toBe('saving');
    http.expectOne('/api/jobs').flush({ ...offer, id: 1 });
    expect(saving.states().get('ext-1')).toBe('saved');
  });

  it('marks a conflict as already saved and prevents another submission', () => {
    saving.save(offer);
    http.expectOne('/api/jobs').flush({}, { status: 409, statusText: 'Conflict' });
    expect(saving.states().get('ext-1')).toBe('duplicate');
    saving.save(offer);
    http.expectNone('/api/jobs');
  });

  it('blocks duplicate attempts but lets different offers save independently', () => {
    saving.save(offer);
    saving.save(offer);
    saving.save({ ...offer, externalId: 'ext-2' });
    const requests = http.match('/api/jobs');
    expect(requests.map((request) => request.request.body.externalId)).toEqual(['ext-1', 'ext-2']);
    requests[1].flush({ ...offer, externalId: 'ext-2', id: 2 });
    expect(saving.states().get('ext-2')).toBe('saved');
    expect(saving.states().get('ext-1')).toBe('saving');
    requests[0].flush({ ...offer, id: 1 });
    saving.save(offer);
    http.expectNone('/api/jobs');
  });

  it('saves allowed offer data and transitions from available to saving to saved', () => {
    expect(saving.states().get('ext-1')).toBeUndefined();
    saving.save({ ...offer, id: 99, createdAt: '2026-10-02T00:00:00Z' });
    expect(saving.states().get('ext-1')).toBe('saving');
    const request = http.expectOne('/api/jobs');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      externalId: 'ext-1',
      title: 'Java Engineer',
      company: 'Acme',
      location: 'Zurich',
      description: 'An excerpt',
      salaryMin: 80000,
      salaryMax: 100000,
      contractType: 'permanent',
      postedDate: '2026-10-01',
      jobUrl: 'https://example.com/job/1',
      requirements: ['Java'],
    });
    request.flush({ ...offer, id: 1 }, { status: 201, statusText: 'Created' });
    expect(saving.states().get('ext-1')).toBe('saved');
  });
});
