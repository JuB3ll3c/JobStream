import {
  CdkDrag,
  CdkDragDrop,
  CdkDragHandle,
  CdkDropList,
  CdkDropListGroup,
} from '@angular/cdk/drag-drop';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY, Subject, catchError, expand, finalize, reduce, startWith, switchMap } from 'rxjs';
import { JobDto, JobService, JobStatus } from '../../../generated';

@Component({
  selector: 'app-saved-jobs',
  imports: [CdkDrag, CdkDragHandle, CdkDropList, CdkDropListGroup],
  templateUrl: './saved-jobs.html',
  styleUrl: './saved-jobs.scss',
})
export class SavedJobs {
  private readonly api = inject(JobService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload = new Subject<void>();
  readonly columns = [
    { status: JobStatus.ToProcess, label: 'À traiter' },
    { status: JobStatus.InProgress, label: 'En cours' },
    { status: JobStatus.Applied, label: 'Postulé / En attente' },
    { status: JobStatus.Done, label: 'Terminé' },
  ];
  readonly jobs = signal<JobDto[]>([]);
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
    const id = event.item.data.id;
    const job = this.jobs().find((job) => job.id === id);
    const target = event.container.data;
    if (id == null || !job || this.pending().has(id) || job.status === target) return;
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
