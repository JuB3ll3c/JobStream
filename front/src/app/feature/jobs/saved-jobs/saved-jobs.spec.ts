import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { By } from '@angular/platform-browser';
import { BASE_PATH, JobDto, JobStatus } from '../../../generated';
import { SavedJobs } from './saved-jobs';

describe('Saved jobs board', () => {
  let fixture: ComponentFixture<SavedJobs>;
  let http: HttpTestingController;
  const offer: JobDto = {
    id: 1,
    externalId: 'ext-1',
    title: 'Java Engineer',
    company: 'Acme',
    location: 'Zurich',
    status: JobStatus.ToProcess,
  };

  beforeEach(async () => {
    Object.defineProperty(HTMLDialogElement.prototype, 'showModal', {
      configurable: true,
      value() {
        this.setAttribute('open', '');
      },
    });
    Object.defineProperty(HTMLDialogElement.prototype, 'close', {
      configurable: true,
      value() {
        this.removeAttribute('open');
        this.dispatchEvent(new Event('close'));
      },
    });
    await TestBed.configureTestingModule({
      imports: [SavedJobs],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: BASE_PATH, useValue: '/api' },
      ],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(SavedJobs);
    fixture.detectChanges();
  });
  afterEach(() => http.verify());

  it('loads every page before showing cards in their status columns', () => {
    http
      .expectOne((req) => req.url === '/api/jobs' && req.params.get('page') === '0')
      .flush({ content: [offer], page: 0, totalPages: 2 });
    http
      .expectOne((req) => req.url === '/api/jobs' && req.params.get('page') === '1')
      .flush({
        content: [{ ...offer, id: 2, title: 'Backend Engineer', status: JobStatus.Applied }],
        page: 1,
        totalPages: 2,
      });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-status="TO_PROCESS"]').textContent).toContain(
      'Java Engineer',
    );
    expect(fixture.nativeElement.querySelector('[data-status="APPLIED"]').textContent).toContain(
      'Backend Engineer',
    );
    expect(fixture.nativeElement.querySelectorAll('.kanban-column').length).toBe(4);
  });
  it('moves a card immediately and saves its generated status through the API', () => {
    http
      .expectOne((req) => req.url === '/api/jobs')
      .flush({ content: [offer], page: 0, totalPages: 1 });
    fixture.detectChanges();
    fixture.debugElement
      .query(By.css('[data-status="IN_PROGRESS"]'))
      .triggerEventHandler('cdkDropListDropped', {
        item: { data: offer },
        container: { data: JobStatus.InProgress },
        previousContainer: { data: JobStatus.ToProcess },
      });
    fixture.detectChanges();
    expect(
      fixture.nativeElement.querySelector('[data-status="IN_PROGRESS"]').textContent,
    ).toContain('Java Engineer');
    const update = http.expectOne('/api/jobs/1/status');
    expect(update.request.method).toBe('PATCH');
    expect(update.request.body).toEqual({ status: 'IN_PROGRESS' });
    update.flush({ ...offer, status: JobStatus.InProgress });
  });

  it('restores only the failed card while allowing another card to be saved', () => {
    const other = { ...offer, id: 2, title: 'Other role' };
    http
      .expectOne((req) => req.url === '/api/jobs')
      .flush({ content: [offer, other], page: 0, totalPages: 1 });
    fixture.detectChanges();
    const target = fixture.debugElement.query(By.css('[data-status="DONE"]'));
    const move = (job: JobDto) =>
      target.triggerEventHandler('cdkDropListDropped', {
        item: { data: job },
        container: { data: JobStatus.Done },
        previousContainer: { data: JobStatus.ToProcess },
      });
    move(offer);
    move(offer);
    move(other);
    const first = http.expectOne('/api/jobs/1/status');
    const second = http.expectOne('/api/jobs/2/status');
    second.flush({ ...other, status: JobStatus.Done });
    first.flush({}, { status: 500, statusText: 'Server error' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-status="TO_PROCESS"]').textContent).toContain(
      'Java Engineer',
    );
    expect(fixture.nativeElement.querySelector('[data-status="DONE"]').textContent).toContain(
      'Other role',
    );
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(
      'Impossible de déplacer',
    );
    move(offer);
    http.expectOne('/api/jobs/1/status').flush({ ...offer, status: JobStatus.Done });
  });

  it('shows a load error instead of a partial collection and can retry', () => {
    expect(fixture.nativeElement.textContent).toContain('Chargement');
    http
      .expectOne((req) => req.url === '/api/jobs')
      .flush({ content: [offer], page: 0, totalPages: 2 });
    http
      .expectOne((req) => req.url === '/api/jobs')
      .flush({}, { status: 500, statusText: 'Server error' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.job-card')).toBeNull();
    expect(fixture.nativeElement.querySelector('[role="alert"]')).not.toBeNull();
    fixture.nativeElement.querySelector('button').click();
    http
      .expectOne((req) => req.url === '/api/jobs' && req.params.get('page') === '0')
      .flush({ content: [], page: 0, totalPages: 0 });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Aucune offre sauvegardée');
  });

  it('cancels pending page requests when leaving the board', () => {
    const request = http.expectOne((req) => req.url === '/api/jobs');
    fixture.destroy();
    expect(request.cancelled).toBe(true);
  });

  it('opens the selected job in a modal and saves status changes on the board', () => {
    http
      .expectOne((req) => req.url === '/api/jobs')
      .flush({ content: [offer], page: 0, totalPages: 1 });
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.job-card h3 button').click();
    fixture.detectChanges();
    const dialog = fixture.nativeElement.querySelector('.detail-dialog') as HTMLDialogElement;
    expect(dialog.open).toBe(true);
    expect(dialog.querySelector('.detail-title .bookmark-button')).not.toBeNull();
    expect(dialog.querySelectorAll('.close-detail')).toHaveLength(1);
    expect(dialog.querySelector('.detail-card')!.textContent).toContain('Java Engineer');
    http.expectNone(() => true);
    const select = dialog.querySelector('select')!;
    select.value = 'APPLIED';
    select.dispatchEvent(new Event('change'));
    const update = http.expectOne('/api/jobs/1/status');
    expect(update.request.body).toEqual({ status: 'APPLIED' });
    select.dispatchEvent(new Event('change'));
    http.expectNone('/api/jobs/1/status');
    fixture.detectChanges();
    expect(select.disabled).toBe(true);
    update.flush({ ...offer, status: JobStatus.Applied });
    fixture.detectChanges();
    expect(select.value).toBe('APPLIED');
    expect(fixture.nativeElement.querySelector('[data-status="APPLIED"]').textContent).toContain(
      'Java Engineer',
    );
    dialog.querySelector<HTMLButtonElement>('.close-detail')!.click();
    fixture.detectChanges();
    expect(dialog.open).toBe(false);
  });
  it('restores the modal status on error and keeps it usable after closing and reopening', () => {
    http
      .expectOne((req) => req.url === '/api/jobs')
      .flush({ content: [offer], page: 0, totalPages: 1 });
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.job-card h3 button').click();
    fixture.detectChanges();
    let dialog = fixture.nativeElement.querySelector('.detail-dialog') as HTMLDialogElement;
    const select = dialog.querySelector('select')!;
    select.value = 'DONE';
    select.dispatchEvent(new Event('change'));
    http
      .expectOne('/api/jobs/1/status')
      .flush({ message: 'private' }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();
    expect(select.value).toBe('TO_PROCESS');
    expect(select.disabled).toBe(false);
    expect(dialog.querySelector('[role="alert"]')!.textContent).toContain('Impossible');
    expect(dialog.textContent).not.toContain('private');
    dialog.close();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.detail-dialog')).toBeNull();
    fixture.nativeElement.querySelector('.job-card h3 button').click();
    fixture.detectChanges();
    dialog = fixture.nativeElement.querySelector('.detail-dialog') as HTMLDialogElement;
    const retry = dialog.querySelector('select')!;
    retry.value = 'DONE';
    retry.dispatchEvent(new Event('change'));
    http.expectOne('/api/jobs/1/status').flush({ ...offer, status: JobStatus.Done });
    fixture.detectChanges();
    expect(retry.value).toBe('DONE');
  });
  it('asks for confirmation and can cancel removal without sending a request', () => {
    http
      .expectOne((req) => req.url === '/api/jobs')
      .flush({ content: [offer], page: 0, totalPages: 1 });
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.job-card h3 button').click();
    fixture.detectChanges();
    const dialog = fixture.nativeElement.querySelector('.detail-dialog') as HTMLDialogElement;
    const bookmark = dialog.querySelector<HTMLButtonElement>('.bookmark-button')!;
    expect(bookmark.getAttribute('aria-label')).toBe(
      'Retirer des offres sauvegardées : Java Engineer',
    );
    expect(bookmark.getAttribute('aria-pressed')).toBe('true');
    expect(bookmark.querySelector('svg[aria-hidden="true"]')).not.toBeNull();
    bookmark.click();
    fixture.detectChanges();
    const confirmation = fixture.nativeElement.querySelector(
      '.removal-confirmation',
    ) as HTMLDialogElement;
    expect(confirmation.open).toBe(true);
    expect(confirmation.textContent).toContain('Retirer « Java Engineer »');
    expect(dialog.querySelector('.removal-confirmation')).toBeNull();
    http.expectNone(() => true);
    confirmation.querySelector<HTMLButtonElement>('.cancel-removal')!.click();
    fixture.detectChanges();
    expect(dialog.open).toBe(true);
    expect(confirmation.open).toBe(false);
    expect(fixture.nativeElement.querySelector('.job-card')).not.toBeNull();
    http.expectNone(() => true);
  });

  it('removes the confirmed offer once, closes its modal and shows an empty collection', () => {
    http
      .expectOne((req) => req.url === '/api/jobs')
      .flush({ content: [offer], page: 0, totalPages: 1 });
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.job-card h3 button').click();
    fixture.detectChanges();
    const dialog = fixture.nativeElement.querySelector('.detail-dialog') as HTMLDialogElement;
    dialog.querySelector<HTMLButtonElement>('.bookmark-button')!.click();
    fixture.detectChanges();
    const confirm = (fixture.nativeElement.querySelector(
      '.removal-confirmation .confirm-removal',
    ) as HTMLButtonElement)!;
    confirm.click();
    confirm.click();
    const removal = http.expectOne('/api/jobs/1');
    expect(removal.request.method).toBe('DELETE');
    fixture.detectChanges();
    expect(confirm.disabled).toBe(true);
    expect(dialog.querySelector('select')!.disabled).toBe(true);
    expect(dialog.querySelector<HTMLButtonElement>('.close-detail')!.disabled).toBe(true);
    const escape = new Event('cancel', { cancelable: true });
    dialog.dispatchEvent(escape);
    expect(escape.defaultPrevented).toBe(true);
    const popup = fixture.nativeElement.querySelector('.removal-confirmation') as HTMLDialogElement;
    const popupEscape = new Event('cancel', { cancelable: true });
    popup.dispatchEvent(popupEscape);
    expect(popupEscape.defaultPrevented).toBe(true);
    removal.flush(null, { status: 204, statusText: 'No Content' });
    fixture.detectChanges();
    expect(dialog.open).toBe(false);
    expect(popup.open).toBe(false);
    expect(fixture.nativeElement.querySelector('.job-card')).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Aucune offre sauvegardée');
  });

  it('preserves the offer on failure and retries removal without changing other cards', () => {
    const other = { ...offer, id: 2, title: 'Other role' };
    http
      .expectOne((req) => req.url === '/api/jobs')
      .flush({ content: [offer, other], page: 0, totalPages: 1 });
    fixture.detectChanges();
    fixture.nativeElement.querySelector('[data-job-id="1"]').click();
    fixture.detectChanges();
    const dialog = fixture.nativeElement.querySelector('.detail-dialog') as HTMLDialogElement;
    dialog.querySelector<HTMLButtonElement>('.bookmark-button')!.click();
    fixture.detectChanges();
    (fixture.nativeElement.querySelector(
      '.removal-confirmation .confirm-removal',
    ) as HTMLButtonElement)!.click();
    http
      .expectOne('/api/jobs/1')
      .flush({ message: 'private details' }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();
    expect(dialog.open).toBe(true);
    expect(
      fixture.nativeElement.querySelector('.removal-confirmation [role="alert"]')!.textContent,
    ).toContain('Impossible de retirer');
    expect(fixture.nativeElement.querySelector('.removal-confirmation').textContent).not.toContain(
      'private details',
    );
    expect(fixture.nativeElement.querySelectorAll('.job-card')).toHaveLength(2);
    expect(
      (fixture.nativeElement.querySelector(
        '.removal-confirmation .confirm-removal',
      ) as HTMLButtonElement)!.disabled,
    ).toBe(false);
    (fixture.nativeElement.querySelector(
      '.removal-confirmation .confirm-removal',
    ) as HTMLButtonElement)!.click();
    http.expectOne('/api/jobs/1').flush(null, { status: 204, statusText: 'No Content' });
    fixture.detectChanges();
    expect(dialog.open).toBe(false);
    const cards = fixture.nativeElement.querySelectorAll('.job-card');
    expect(cards).toHaveLength(1);
    expect(cards[0].textContent).toContain('Other role');
  });

  it('prevents removal while a status update is pending and vice versa', () => {
    http
      .expectOne((req) => req.url === '/api/jobs')
      .flush({ content: [offer], page: 0, totalPages: 1 });
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.job-card h3 button').click();
    fixture.detectChanges();
    const dialog = fixture.nativeElement.querySelector('.detail-dialog') as HTMLDialogElement;
    const select = dialog.querySelector('select')!;
    select.value = 'APPLIED';
    select.dispatchEvent(new Event('change'));
    const update = http.expectOne('/api/jobs/1/status');
    fixture.detectChanges();
    const request = dialog.querySelector<HTMLButtonElement>('.bookmark-button')!;
    expect(request.disabled).toBe(true);
    request.click();
    expect(dialog.querySelector('.confirm-removal')).toBeNull();
    update.flush({ ...offer, status: JobStatus.Applied });
    fixture.detectChanges();
    request.click();
    fixture.detectChanges();
    (fixture.nativeElement.querySelector(
      '.removal-confirmation .confirm-removal',
    ) as HTMLButtonElement)!.click();
    const removal = http.expectOne('/api/jobs/1');
    select.value = 'DONE';
    select.dispatchEvent(new Event('change'));
    fixture.debugElement
      .query(By.css('[data-status="DONE"]'))
      .triggerEventHandler('cdkDropListDropped', {
        item: { data: offer },
        container: { data: JobStatus.Done },
      });
    http.expectNone('/api/jobs/1/status');
    removal.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('clears an abandoned confirmation and cancels a pending removal on page destruction', () => {
    http
      .expectOne((req) => req.url === '/api/jobs')
      .flush({ content: [offer], page: 0, totalPages: 1 });
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.job-card h3 button').click();
    fixture.detectChanges();
    let dialog = fixture.nativeElement.querySelector('.detail-dialog') as HTMLDialogElement;
    dialog.querySelector<HTMLButtonElement>('.bookmark-button')!.click();
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('.removal-confirmation') as HTMLDialogElement).close();
    dialog.close();
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.job-card h3 button').click();
    fixture.detectChanges();
    dialog = fixture.nativeElement.querySelector('.detail-dialog') as HTMLDialogElement;
    expect(dialog.querySelector('.confirm-removal')).toBeNull();
    dialog.querySelector<HTMLButtonElement>('.bookmark-button')!.click();
    fixture.detectChanges();
    (fixture.nativeElement.querySelector(
      '.removal-confirmation .confirm-removal',
    ) as HTMLButtonElement)!.click();
    const removal = http.expectOne('/api/jobs/1');
    fixture.destroy();
    expect(removal.cancelled).toBe(true);
  });
});
