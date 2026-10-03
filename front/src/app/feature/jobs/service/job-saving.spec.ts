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
    saving.toggle(offer);
    http.expectOne('/api/jobs').flush({}, { status: 500, statusText: 'Error' });
    expect(saving.states().get('ext-1')).toBe('error');
    saving.toggle(offer);
    expect(saving.states().get('ext-1')).toBe('saving');
    http.expectOne('/api/jobs').flush({ ...offer, id: 1 });
    expect(saving.states().get('ext-1')).toBe('saved');
  });

  it('removes a saved offer on the next click and allows saving it again', () => {
    saving.toggle(offer);
    http.expectOne('/api/jobs').flush({ ...offer, id: 1 });
    saving.toggle(offer);
    const removal = http.expectOne('/api/jobs/1');
    expect(removal.request.method).toBe('DELETE');
    expect(saving.states().get('ext-1')).toBe('removing');
    saving.toggle(offer);
    http.expectNone('/api/jobs/1');
    removal.flush(null, { status: 204, statusText: 'No Content' });
    expect(saving.states().get('ext-1')).toBeUndefined();
    saving.toggle(offer);
    http.expectOne('/api/jobs').flush({ ...offer, id: 2 });
  });

  it('looks up an already saved offer before removing it on the next click', () => {
    saving.toggle(offer);
    http.expectOne('/api/jobs').flush({}, { status: 409, statusText: 'Conflict' });
    expect(saving.states().get('ext-1')).toBe('duplicate');
    saving.toggle(offer);
    expect(saving.states().get('ext-1')).toBe('removing');
    saving.toggle(offer);
    http.expectOne('/api/jobs/by-external-id/ext-1').flush({ ...offer, id: 42 });
    const removal = http.expectOne('/api/jobs/42');
    expect(removal.request.method).toBe('DELETE');
    removal.flush(null, { status: 204, statusText: 'No Content' });
    expect(saving.states().get('ext-1')).toBeUndefined();
    http.expectNone('/api/jobs');
  });

  it('blocks duplicate attempts but lets different offers save independently', () => {
    saving.toggle(offer);
    saving.toggle(offer);
    saving.toggle({ ...offer, externalId: 'ext-2' });
    const requests = http.match('/api/jobs');
    expect(requests.map((request) => request.request.body.externalId)).toEqual(['ext-1', 'ext-2']);
    requests[1].flush({ ...offer, externalId: 'ext-2', id: 2 });
    expect(saving.states().get('ext-2')).toBe('saved');
    expect(saving.states().get('ext-1')).toBe('saving');
    requests[0].flush({ ...offer, id: 1 });
    http.expectNone('/api/jobs');
  });

  it('keeps the offer saved after a failed removal and allows retrying', () => {
    saving.toggle(offer);
    http.expectOne('/api/jobs').flush({ ...offer, id: 1 });
    saving.toggle(offer);
    http.expectOne('/api/jobs/1').flush({}, { status: 500, statusText: 'Error' });
    expect(saving.states().get('ext-1')).toBe('remove-error');
    saving.toggle(offer);
    http.expectOne('/api/jobs/1').flush(null, { status: 204, statusText: 'No Content' });
    expect(saving.states().get('ext-1')).toBeUndefined();
  });

  it('saves allowed offer data and transitions from available to saving to saved', () => {
    expect(saving.states().get('ext-1')).toBeUndefined();
    saving.toggle({ ...offer, id: 99, createdAt: '2026-10-02T00:00:00Z' });
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

  it('treats a missing offer during removal as already removed', () => {
    saving.toggle(offer);
    http.expectOne('/api/jobs').flush({ ...offer, id: 1 });
    saving.toggle(offer);
    http.expectOne('/api/jobs/1').flush({}, { status: 404, statusText: 'Not Found' });
    expect(saving.states().get('ext-1')).toBeUndefined();
  });

  it('clears the bookmark when an already saved offer is missing at lookup', () => {
    saving.toggle(offer);
    http.expectOne('/api/jobs').flush({}, { status: 409, statusText: 'Conflict' });
    saving.toggle(offer);
    http
      .expectOne('/api/jobs/by-external-id/ext-1')
      .flush({}, { status: 404, statusText: 'Not Found' });
    expect(saving.states().get('ext-1')).toBeUndefined();
    http.expectNone((request) => request.method === 'DELETE');
  });

  it('retries a failed lookup without sending another save', () => {
    saving.toggle(offer);
    http.expectOne('/api/jobs').flush({}, { status: 409, statusText: 'Conflict' });
    saving.toggle(offer);
    http
      .expectOne('/api/jobs/by-external-id/ext-1')
      .flush({}, { status: 500, statusText: 'Error' });
    expect(saving.states().get('ext-1')).toBe('remove-error');
    saving.toggle(offer);
    http.expectOne('/api/jobs/by-external-id/ext-1').flush({ ...offer, id: 42 });
    http.expectOne('/api/jobs/42').flush(null, { status: 204, statusText: 'No Content' });
    http.expectNone('/api/jobs');
  });

  it('does not send a deletion when the lookup response has no internal ID', () => {
    saving.toggle(offer);
    http.expectOne('/api/jobs').flush({}, { status: 409, statusText: 'Conflict' });
    saving.toggle(offer);
    http.expectOne('/api/jobs/by-external-id/ext-1').flush(offer);
    expect(saving.states().get('ext-1')).toBe('remove-error');
    http.expectNone((request) => request.method === 'DELETE');
  });

  it('cancels a pending lookup when the page-scoped service is destroyed', () => {
    saving.toggle(offer);
    http.expectOne('/api/jobs').flush({}, { status: 409, statusText: 'Conflict' });
    saving.toggle(offer);
    const lookup = http.expectOne('/api/jobs/by-external-id/ext-1');
    TestBed.resetTestingModule();
    expect(lookup.cancelled).toBe(true);
  });

  it('cancels a pending deletion when the page-scoped service is destroyed', () => {
    saving.toggle(offer);
    http.expectOne('/api/jobs').flush({ ...offer, id: 1 });
    saving.toggle(offer);
    const removal = http.expectOne('/api/jobs/1');
    TestBed.resetTestingModule();
    expect(removal.cancelled).toBe(true);
  });
});
