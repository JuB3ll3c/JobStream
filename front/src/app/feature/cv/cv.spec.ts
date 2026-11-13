import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BASE_PATH, CvMetadataDto } from '../../generated';
import { Cv } from './cv';

describe('Cv page', () => {
  let fixture: ComponentFixture<Cv>;
  let http: HttpTestingController;
  const current: CvMetadataDto = {
    originalFilename: 'mon-cv.pdf',
    sizeBytes: 128000,
    uploadedAt: '2026-11-13T10:00:00',
    version: 1,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Cv],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: BASE_PATH, useValue: '/api' },
      ],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Cv);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  function selectFile(file: File): void {
    const input: HTMLInputElement = fixture.nativeElement.querySelector('input[type="file"]');
    Object.defineProperty(input, 'files', { value: { item: () => file }, configurable: true });
    input.dispatchEvent(new Event('change'));
    fixture.detectChanges();
  }

  function submit(): void {
    fixture.nativeElement
      .querySelector('form')
      .dispatchEvent(new Event('submit', { cancelable: true }));
    fixture.detectChanges();
  }

  it('loads and displays the current CV filename, size and import date', () => {
    expect(fixture.nativeElement.textContent).toContain('Chargement');
    const request = http.expectOne('/api/me/cv');
    expect(request.request.method).toBe('GET');
    request.flush(current);
    fixture.detectChanges();
    const page: HTMLElement = fixture.nativeElement;
    expect(page.textContent).toContain('mon-cv.pdf');
    expect(page.textContent).toContain('128 ko');
    expect(page.textContent).toContain('13/11/2026');
    expect(page.textContent).not.toContain('Chargement');
  });

  it('treats a missing CV as an empty state rather than an error', () => {
    http.expectOne('/api/me/cv').flush({}, { status: 404, statusText: 'Not Found' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Aucun CV importé');
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
  });

  it('allows retrying a failed load without showing server details', () => {
    http
      .expectOne('/api/me/cv')
      .flush({ message: 'internal details' }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(
      'Impossible de charger',
    );
    expect(fixture.nativeElement.textContent).not.toContain('internal details');
    fixture.nativeElement.querySelector('button').click();
    http.expectOne('/api/me/cv').flush(current);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('mon-cv.pdf');
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
  });

  it.each([false, true])(
    'imports a PDF and displays the returned metadata (replacement: %s)',
    (replacement) => {
      const initial = http.expectOne('/api/me/cv');
      if (replacement) initial.flush(current);
      else initial.flush({}, { status: 404, statusText: 'Not Found' });
      fixture.detectChanges();
      const file = new File(['%PDF-example'], 'nouveau-cv.pdf', { type: 'application/pdf' });
      selectFile(file);
      submit();
      submit();
      const request = http.expectOne('/api/me/cv');
      expect(request.request.method).toBe('PUT');
      expect(request.request.body).toBeInstanceOf(FormData);
      const sent = request.request.body.get('file') as File;
      expect(sent.name).toBe('nouveau-cv.pdf');
      expect(sent.size).toBe(file.size);
      expect(sent.type).toBe('application/pdf');
      expect(request.request.headers.has('Content-Type')).toBe(false);
      expect(fixture.nativeElement.textContent).toContain('Envoi en cours');
      expect(fixture.nativeElement.querySelector('button[type="submit"]').disabled).toBe(true);
      const updated = { ...current, originalFilename: file.name, version: replacement ? 2 : 1 };
      request.flush(updated, { status: replacement ? 200 : 201, statusText: 'OK' });
      fixture.detectChanges();
      expect(fixture.nativeElement.textContent).toContain('nouveau-cv.pdf');
      expect(fixture.nativeElement.textContent).toContain('CV enregistré');
      expect(fixture.nativeElement.querySelector('button[type="submit"]').disabled).toBe(true);
      expect(fixture.nativeElement.querySelector('input[type="file"]').value).toBe('');
    },
  );

  it.each([
    [0, 'application/pdf', 'vide'],
    [5000001, 'application/pdf', '5 Mo'],
    [10, 'image/png', 'PDF'],
    [10, '', 'PDF'],
  ])('rejects a file of size %s and type %s before uploading', (size, type, message) => {
    http.expectOne('/api/me/cv').flush(current);
    fixture.detectChanges();
    selectFile(new File([new Uint8Array(size as number)], 'cv.pdf', { type: type as string }));
    submit();
    http.expectNone('/api/me/cv');
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(message);
    expect(fixture.nativeElement.textContent).toContain('mon-cv.pdf');
    expect(fixture.nativeElement.querySelector('button[type="submit"]').disabled).toBe(true);
  });

  it('accepts a PDF of exactly 5,000,000 bytes', () => {
    http.expectOne('/api/me/cv').flush(current);
    fixture.detectChanges();
    selectFile(new File([new Uint8Array(5000000)], 'cv.pdf', { type: 'application/pdf' }));
    submit();
    http.expectOne('/api/me/cv').flush({ ...current, sizeBytes: 5000000, version: 2 });
  });

  it.each([
    [400, 'PDF'],
    [413, '5 Mo'],
    [415, 'PDF'],
    [500, 'Réessayez'],
  ])('keeps the current CV and allows retrying after an upload error %s', (status, message) => {
    http.expectOne('/api/me/cv').flush(current);
    fixture.detectChanges();
    selectFile(new File(['pdf'], 'replacement.pdf', { type: 'application/pdf' }));
    submit();
    http
      .expectOne('/api/me/cv')
      .flush({ message: 'internal details' }, { status, statusText: 'Error' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('mon-cv.pdf');
    expect(fixture.nativeElement.textContent).not.toContain('internal details');
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(message);
    expect(fixture.nativeElement.querySelector('button[type="submit"]').disabled).toBe(false);
    submit();
    http
      .expectOne('/api/me/cv')
      .flush({ ...current, originalFilename: 'replacement.pdf', version: 2 });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('replacement.pdf');
  });

  it('clears a file validation error when a valid PDF is selected', () => {
    http.expectOne('/api/me/cv').flush(current);
    fixture.detectChanges();
    selectFile(new File([], 'empty.pdf', { type: 'application/pdf' }));
    expect(fixture.nativeElement.querySelector('[role="alert"]')).not.toBeNull();
    selectFile(new File(['pdf'], 'valid.pdf', { type: 'application/pdf' }));
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
    expect(fixture.nativeElement.querySelector('button[type="submit"]').disabled).toBe(false);
  });

  it('cancels the metadata request when the page is destroyed', () => {
    const request = http.expectOne('/api/me/cv');
    fixture.destroy();
    expect(request.cancelled).toBe(true);
  });

  it('cancels a pending upload when the page is destroyed', () => {
    http.expectOne('/api/me/cv').flush(current);
    fixture.detectChanges();
    selectFile(new File(['pdf'], 'cv.pdf', { type: 'application/pdf' }));
    submit();
    const request = http.expectOne('/api/me/cv');
    fixture.destroy();
    expect(request.cancelled).toBe(true);
  });
});
