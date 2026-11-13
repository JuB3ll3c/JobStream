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

  afterEach(() => {
    http.verify();
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it('asks for confirmation and allows cancelling without sending a delete request', () => {
    http.expectOne('/api/me/cv').flush(current);
    fixture.detectChanges();
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('.cv-delete');
    expect(button).not.toBeNull();
    button.click();
    fixture.detectChanges();
    const dialog: HTMLDialogElement = fixture.nativeElement.querySelector('dialog');
    expect(dialog.open).toBe(true);
    expect(button.disabled).toBe(false);
    expect(dialog.textContent).toContain('mon-cv.pdf');
    http.expectNone('/api/me/cv');
    dialog.querySelector<HTMLButtonElement>('.cancel-removal')!.click();
    fixture.detectChanges();
    expect(dialog.open).toBe(false);
    http.expectNone('/api/me/cv');
    expect(fixture.nativeElement.textContent).toContain('mon-cv.pdf');
  });

  function browserDownload() {
    const create = vi.fn().mockReturnValue('blob:cv-pdf');
    const revoke = vi.fn();
    const NativeURL = URL;
    vi.stubGlobal(
      'URL',
      class extends NativeURL {
        static override createObjectURL = create;
        static override revokeObjectURL = revoke;
      },
    );
    const links: { href: string; filename: string; connected: boolean }[] = [];
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(function (
      this: HTMLAnchorElement,
    ) {
      links.push({ href: this.href, filename: this.download, connected: this.isConnected });
    });
    return { create, revoke, links };
  }

  it('deletes only after confirmation, prevents duplicate requests and returns to the empty state', () => {
    http.expectOne('/api/me/cv').flush(current);
    fixture.detectChanges();
    selectFile(new File(['pdf'], 'replacement.pdf', { type: 'application/pdf' }));
    fixture.nativeElement.querySelector('.cv-delete').click();
    fixture.detectChanges();
    const dialog: HTMLDialogElement = fixture.nativeElement.querySelector('dialog');
    const confirm = dialog.querySelector<HTMLButtonElement>('.confirm-removal')!;
    confirm.click();
    confirm.click();
    fixture.detectChanges();
    const request = http.expectOne('/api/me/cv');
    expect(request.request.method).toBe('DELETE');
    expect(confirm.disabled).toBe(true);
    expect(dialog.querySelector<HTMLButtonElement>('.cancel-removal')!.disabled).toBe(true);
    expect(dialog.textContent).toContain('Suppression en cours');
    const escape = new Event('cancel', { cancelable: true });
    dialog.dispatchEvent(escape);
    expect(escape.defaultPrevented).toBe(true);
    request.flush(null, { status: 204, statusText: 'No Content' });
    fixture.detectChanges();
    expect(dialog.open).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('Aucun CV importé');
    expect(fixture.nativeElement.querySelector('.cv-delete')).toBeNull();
    expect(fixture.nativeElement.querySelector('.cv-download')).toBeNull();
    expect(fixture.nativeElement.querySelector('button[type="submit"]').disabled).toBe(true);
    expect(fixture.nativeElement.querySelector('input[type="file"]').value).toBe('');
    expect(document.activeElement).toBe(fixture.nativeElement.querySelector('h1'));
  });

  it.each([
    [404, 'plus disponible'],
    [500, 'Réessayez'],
  ])('keeps the CV and confirmation available after a deletion error %s', (status, message) => {
    http.expectOne('/api/me/cv').flush(current);
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.cv-delete').click();
    fixture.detectChanges();
    const dialog: HTMLDialogElement = fixture.nativeElement.querySelector('dialog');
    const confirm = dialog.querySelector<HTMLButtonElement>('.confirm-removal')!;
    confirm.click();
    http
      .expectOne('/api/me/cv')
      .flush({ message: 'internal details' }, { status, statusText: 'Error' });
    fixture.detectChanges();
    expect(dialog.open).toBe(true);
    expect(dialog.querySelector('[role="alert"]')!.textContent).toContain(message);
    expect(dialog.textContent).not.toContain('internal details');
    expect(confirm.disabled).toBe(false);
    expect(dialog.querySelector<HTMLButtonElement>('.cancel-removal')!.disabled).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('mon-cv.pdf');
    confirm.click();
    fixture.detectChanges();
    expect(dialog.querySelector('[role="alert"]')).toBeNull();
    http.expectOne('/api/me/cv').flush(null, { status: 204, statusText: 'No Content' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Aucun CV importé');
  });

  it('blocks replacement and download while the confirmation is open', () => {
    http.expectOne('/api/me/cv').flush(current);
    fixture.detectChanges();
    selectFile(new File(['pdf'], 'replacement.pdf', { type: 'application/pdf' }));
    fixture.nativeElement.querySelector('.cv-delete').click();
    fixture.detectChanges();
    submit();
    fixture.nativeElement.querySelector('.cv-download').click();
    http.expectNone('/api/me/cv');
    http.expectNone('/api/me/cv/file');
  });

  it('cancels a pending deletion request when the page is destroyed', () => {
    http.expectOne('/api/me/cv').flush(current);
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.cv-delete').click();
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.confirm-removal').click();
    const request = http.expectOne('/api/me/cv');
    fixture.destroy();
    expect(request.cancelled).toBe(true);
  });

  it('downloads the returned PDF using its UTF-8 response filename and releases the object URL', async () => {
    const download = browserDownload();
    http.expectOne('/api/me/cv').flush(current);
    fixture.detectChanges();
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('.cv-download');
    expect(button).not.toBeNull();
    button.click();
    fixture.detectChanges();
    button.click();
    const request = http.expectOne('/api/me/cv/file');
    expect(request.request.method).toBe('GET');
    expect(request.request.responseType).toBe('blob');
    expect(button.disabled).toBe(true);
    expect(button.textContent).toContain('Téléchargement');
    const pdf = new Blob(['original PDF bytes'], { type: 'application/pdf' });
    request.flush(pdf, {
      headers: { 'Content-Disposition': "attachment; filename*=UTF-8''CV%20%C3%A9t%C3%A9.pdf" },
    });
    fixture.detectChanges();
    expect(download.create).toHaveBeenCalledWith(pdf);
    expect(download.links).toEqual([
      { href: 'blob:cv-pdf', filename: 'CV été.pdf', connected: true },
    ]);
    expect(document.querySelector('a[download]')).toBeNull();
    expect(button.disabled).toBe(false);
    await vi.waitFor(() => expect(download.revoke).toHaveBeenCalledWith('blob:cv-pdf'));
  });

  it.each([null, "attachment; filename*=UTF-8''%E0%A4%A"])(
    'uses the displayed filename when the download header is missing or invalid: %s',
    async (disposition) => {
      const download = browserDownload();
      http.expectOne('/api/me/cv').flush(current);
      fixture.detectChanges();
      fixture.nativeElement.querySelector('.cv-download').click();
      http.expectOne('/api/me/cv/file').flush(new Blob(['pdf'], { type: 'application/pdf' }), {
        headers: disposition ? { 'Content-Disposition': disposition } : {},
      });
      expect(download.links[0]?.filename).toBe('mon-cv.pdf');
      await vi.waitFor(() => expect(download.revoke).toHaveBeenCalledWith('blob:cv-pdf'));
    },
  );

  it.each([
    [404, 'plus disponible'],
    [500, 'Réessayez'],
  ])(
    'shows a download error %s and allows retrying without losing the current metadata',
    async (status, message) => {
      const download = browserDownload();
      http.expectOne('/api/me/cv').flush(current);
      fixture.detectChanges();
      const button: HTMLButtonElement = fixture.nativeElement.querySelector('.cv-download');
      button.click();
      http
        .expectOne('/api/me/cv/file')
        .flush(new Blob(['internal details'], { type: 'application/json' }), {
          status,
          statusText: 'Error',
        });
      fixture.detectChanges();
      expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(message);
      expect(fixture.nativeElement.textContent).not.toContain('internal details');
      expect(fixture.nativeElement.textContent).toContain('mon-cv.pdf');
      expect(button.disabled).toBe(false);
      expect(download.create).not.toHaveBeenCalled();
      button.click();
      fixture.detectChanges();
      expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
      http.expectOne('/api/me/cv/file').flush(new Blob(['pdf'], { type: 'application/pdf' }));
      await vi.waitFor(() => expect(download.revoke).toHaveBeenCalledWith('blob:cv-pdf'));
    },
  );

  it('cancels a pending download when leaving the page without creating a browser file', () => {
    const download = browserDownload();
    http.expectOne('/api/me/cv').flush(current);
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.cv-download').click();
    const request = http.expectOne('/api/me/cv/file');
    fixture.destroy();
    expect(request.cancelled).toBe(true);
    expect(download.create).not.toHaveBeenCalled();
  });

  it('disables downloading during replacement and clears a previous download error on success', () => {
    http.expectOne('/api/me/cv').flush(current);
    fixture.detectChanges();
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('.cv-download');
    button.click();
    http.expectOne('/api/me/cv/file').flush(null, { status: 500, statusText: 'Error' });
    selectFile(new File(['pdf'], 'replacement.pdf', { type: 'application/pdf' }));
    submit();
    expect(button.disabled).toBe(true);
    button.click();
    http.expectNone('/api/me/cv/file');
    http
      .expectOne('/api/me/cv')
      .flush({ ...current, originalFilename: 'replacement.pdf', version: 2 });
    fixture.detectChanges();
    expect(button.disabled).toBe(false);
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
  });

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
    expect(fixture.nativeElement.querySelector('.cv-download')).toBeNull();
    expect(fixture.nativeElement.querySelector('.cv-delete')).toBeNull();
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
