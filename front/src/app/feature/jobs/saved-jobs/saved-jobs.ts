import {
  CdkDrag,
  CdkDragDrop,
  CdkDragHandle,
  CdkDropList,
  CdkDropListGroup,
} from '@angular/cdk/drag-drop';
import {
  Component,
  DestroyRef,
  Injector,
  afterNextRender,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY, Subject, catchError, expand, finalize, reduce, startWith, switchMap } from 'rxjs';
import { JobDetail } from '../job-detail/job-detail';
import { JobDto, JobService, JobStatus } from '../../../generated';

@Component({
  selector: 'app-saved-jobs',
  imports: [JobDetail, CdkDrag, CdkDragHandle, CdkDropList, CdkDropListGroup],
  templateUrl: './saved-jobs.html',
  styleUrl: './saved-jobs.scss',
})
export class SavedJobs {
  private readonly api = inject(JobService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly injector = inject(Injector);
  private readonly reload = new Subject<void>();
  readonly columns = [
    { status: JobStatus.ToProcess, label: 'À traiter' },
    { status: JobStatus.InProgress, label: 'En cours' },
    { status: JobStatus.Applied, label: 'Postulé / En attente' },
    { status: JobStatus.Done, label: 'Terminé' },
  ];
  readonly jobs = signal<JobDto[]>([]);
  readonly confirmingRemoval = signal(false);
  readonly removing = signal(false);
  readonly removalError = signal<string | null>(null);

  requestRemoval(dialog: HTMLDialogElement): void {
    const id = this.selectedId();
    if (id == null || this.pending().has(id) || this.removing() || this.confirmingRemoval()) return;
    this.confirmingRemoval.set(true);
    this.removalError.set(null);
    dialog.showModal();
  }

  confirmRemoval(dialog: HTMLDialogElement, board: HTMLElement, heading: HTMLHeadingElement): void {
    const id = this.selectedId();
    if (id == null || !this.confirmingRemoval() || this.removing() || this.pending().has(id))
      return;
    this.removing.set(true);
    this.removalError.set(null);
    this.api
      .deleteJob({ id })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.removing.set(false)),
      )
      .subscribe({
        next: () => {
          dialog.close();
          this.jobs.update((jobs) => jobs.filter((job) => job.id !== id));
          this.removing.set(false);
          this.closeDetail(board, heading);
        },
        error: () => this.removalError.set('Impossible de retirer cette offre. Réessayez.'),
      });
  }

  readonly selectedId = signal<number | null>(null);
  readonly selectedJob = computed(
    () => this.jobs().find((job) => job.id === this.selectedId()) ?? null,
  );

  openDetail(job: JobDto): void {
    if (job.id == null || this.removing()) return;
    this.confirmingRemoval.set(false);
    this.removalError.set(null);
    this.selectedId.set(job.id);
    this.moveError.set(null);
  }

  closeDetail(board: HTMLElement, heading: HTMLHeadingElement): void {
    const id = this.selectedId();
    this.confirmingRemoval.set(false);
    this.removalError.set(null);
    this.selectedId.set(null);
    afterNextRender(
      () => {
        if (!board.isConnected) return;
        const opener = this.jobs().some((job) => job.id === id)
          ? board.querySelector<HTMLButtonElement>('[data-job-id="' + id + '"]')
          : null;
        (opener ?? heading).focus();
      },
      { injector: this.injector },
    );
  }

  selectStatus(select: HTMLSelectElement, job: JobDto): void {
    const target = this.columns.find((column) => column.status === select.value)?.status;
    select.value = job.status ?? '';
    if (target) this.updateStatus(job, target);
  }

  readonly pending = signal<ReadonlySet<number>>(new Set());
  readonly moveError = signal<string | null>(null);
  readonly announcement = signal('');
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() {
    this.reload
      .pipe(
        startWith(undefined),
        switchMap(() => {
          this.jobs.set([]);
          this.error.set(null);
          this.loading.set(true);
          const page = (index: number) =>
            this.api.getJobs({ page: index, size: 100, sort: ['createdAt,desc', 'id,desc'] });
          return page(0).pipe(
            expand((response, index) =>
              index + 1 < (response.totalPages ?? 0) ? page(index + 1) : EMPTY,
            ),
            reduce((jobs, response) => [...jobs, ...(response.content ?? [])], [] as JobDto[]),
            catchError(() => {
              this.error.set('Impossible de charger vos offres. Réessayez.');
              return EMPTY;
            }),
            finalize(() => this.loading.set(false)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((jobs) => this.jobs.set(jobs));
  }

  drop(event: CdkDragDrop<JobStatus, JobStatus, JobDto>): void {
    this.updateStatus(event.item.data, event.container.data);
  }

  private updateStatus(offer: JobDto, target: JobStatus): void {
    const id = offer.id;
    const job = this.jobs().find((job) => job.id === id);
    if (
      id == null ||
      !job ||
      this.pending().has(id) ||
      job.status === target ||
      (this.selectedId() === id && (this.confirmingRemoval() || this.removing()))
    )
      return;
    const previous = job.status;
    this.moveError.set(null);
    this.pending.update((ids) => new Set([...ids, id]));
    this.jobs.update((jobs) =>
      jobs.map((item) => (item.id === id ? { ...item, status: target } : item)),
    );
    this.api
      .updateJobStatus({ id, jobStatusUpdateRequest: { status: target } })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() =>
          this.pending.update((ids) => {
            const remaining = new Set(ids);
            remaining.delete(id);
            return remaining;
          }),
        ),
      )
      .subscribe({
        next: (updated) => {
          this.jobs.update((jobs) => jobs.map((item) => (item.id === id ? updated : item)));
          this.announcement.set('Statut de « ' + job.title + ' » enregistré.');
        },
        error: () => {
          this.jobs.update((jobs) =>
            jobs.map((item) => (item.id === id ? { ...item, status: previous } : item)),
          );
          this.moveError.set('Impossible de déplacer « ' + job.title + ' ». Réessayez.');
        },
      });
  }

  retry(): void {
    this.reload.next();
  }

  jobsFor(status: JobStatus): JobDto[] {
    return this.jobs().filter((job) => job.status === status);
  }
}
