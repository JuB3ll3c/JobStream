import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, of, switchMap, throwError } from 'rxjs';
import { JobDto, JobRequestDto, JobService } from '../../../generated';

type SaveState = 'saving' | 'saved' | 'duplicate' | 'error' | 'removing' | 'remove-error';

@Injectable()
export class JobSaving {
  private readonly api = inject(JobService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly state = signal<ReadonlyMap<string, SaveState>>(new Map());
  readonly states = this.state.asReadonly();
  private readonly savedIds = new Map<string, number>();

  toggle(offer: JobDto): void {
    const state = this.states().get(offer.externalId);
    if (state === 'saved' || state === 'duplicate' || state === 'remove-error') {
      this.remove(offer.externalId);
      return;
    }
    if (state && state !== 'error') return;
    this.setState(offer.externalId, 'saving');
    const request: JobRequestDto = {
      externalId: offer.externalId,
      title: offer.title,
      company: offer.company,
      location: offer.location,
      description: offer.description,
      salaryMin: offer.salaryMin,
      salaryMax: offer.salaryMax,
      contractType: offer.contractType,
      postedDate: offer.postedDate,
      jobUrl: offer.jobUrl,
      requirements: offer.requirements,
    };
    this.api
      .saveJob({ jobRequestDto: request })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (saved) => {
          if (saved.id != null) this.savedIds.set(offer.externalId, saved.id);
          this.setState(offer.externalId, 'saved');
        },
        error: (error: HttpErrorResponse) => {
          this.setState(offer.externalId, error.status === 409 ? 'duplicate' : 'error');
        },
      });
  }

  private remove(externalId: string): void {
    this.setState(externalId, 'removing');
    const id = this.savedIds.get(externalId);
    const saved: Observable<{ id?: number }> =
      id == null ? this.api.getJobByExternalId({ externalId }) : of({ id });
    saved
      .pipe(
        switchMap((job) =>
          job.id == null
            ? throwError(() => new Error('Missing saved job ID'))
            : this.api.deleteJob({ id: job.id }),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.clearState(externalId);
        },
        error: (error: HttpErrorResponse) => {
          if (error.status === 404) this.clearState(externalId);
          else this.setState(externalId, 'remove-error');
        },
      });
  }

  private clearState(externalId: string): void {
    this.savedIds.delete(externalId);
    this.state.update((states) => {
      const updated = new Map(states);
      updated.delete(externalId);
      return updated;
    });
  }

  private setState(externalId: string, state: SaveState): void {
    this.state.update((states) => new Map(states).set(externalId, state));
  }
}
