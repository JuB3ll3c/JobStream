import { TestBed } from '@angular/core/testing';
import { JobDetail } from './job-detail';

describe('JobDetail presentation', () => {
  beforeEach(() => {
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
  });
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
    expect(fixture.nativeElement.querySelector('select')).toBeNull();
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
  it('owns the modal, close control and header bookmark without services', async () => {
    await TestBed.configureTestingModule({ imports: [JobDetail] }).compileComponents();
    const fixture = TestBed.createComponent(JobDetail);
    fixture.componentRef.setInput('job', {
      externalId: 'one',
      title: 'Engineer',
      company: 'Acme',
      location: 'Zurich',
    });
    fixture.componentRef.setInput('bookmarked', true);
    const bookmark = vi.fn();
    const closed = vi.fn();
    fixture.componentInstance.bookmarkToggle.subscribe(bookmark);
    fixture.componentInstance.closed.subscribe(closed);
    fixture.detectChanges();
    const dialog = fixture.nativeElement.querySelector('dialog') as HTMLDialogElement;
    expect(dialog.open).toBe(true);
    expect(dialog.getAttribute('aria-label')).toBe('Engineer');
    const action = dialog.querySelector<HTMLButtonElement>('.detail-title .bookmark-button')!;
    expect(action.getAttribute('aria-pressed')).toBe('true');
    action.click();
    expect(bookmark).toHaveBeenCalledOnce();
    dialog.querySelector<HTMLButtonElement>('.close-detail')!.click();
    expect(dialog.open).toBe(false);
    expect(closed).toHaveBeenCalledOnce();
  });

  it('honors disabled actions and releases the modal when destroyed without emitting close', async () => {
    await TestBed.configureTestingModule({ imports: [JobDetail] }).compileComponents();
    const fixture = TestBed.createComponent(JobDetail);
    fixture.componentRef.setInput('job', {
      externalId: 'one',
      title: 'Engineer',
      company: 'Acme',
      location: 'Zurich',
    });
    fixture.componentRef.setInput('closeDisabled', true);
    fixture.componentRef.setInput('bookmarkBusy', true);
    const bookmark = vi.fn();
    const closed = vi.fn();
    fixture.componentInstance.bookmarkToggle.subscribe(bookmark);
    fixture.componentInstance.closed.subscribe(closed);
    fixture.detectChanges();
    const dialog = fixture.nativeElement.querySelector('dialog') as HTMLDialogElement;
    dialog.querySelector<HTMLButtonElement>('.bookmark-button')!.click();
    dialog.querySelector<HTMLButtonElement>('.close-detail')!.click();
    expect(dialog.open).toBe(true);
    expect(bookmark).not.toHaveBeenCalled();
    const escape = new Event('cancel', { cancelable: true });
    dialog.dispatchEvent(escape);
    expect(escape.defaultPrevented).toBe(true);
    fixture.destroy();
    expect(dialog.open).toBe(false);
    expect(closed).not.toHaveBeenCalled();
  });
});
