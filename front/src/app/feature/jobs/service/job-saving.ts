import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { JobDto, JobRequestDto, JobService } from '../../../generated';

type SaveState = 'saving' | 'saved' | 'duplicate' | 'error';

@Injectable()
export class JobSaving {
  private readonly api = inject(JobService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly state = signal<ReadonlyMap<string, SaveState>>(new Map());
  readonly states = this.state.asReadonly();

  save(offer: JobDto): void {
    const state = this.states().get(offer.externalId);
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
        next: () => this.setState(offer.externalId, 'saved'),
        error: (error: HttpErrorResponse) => {
          this.setState(offer.externalId, error.status === 409 ? 'duplicate' : 'error');
        },
      });
  }

  private setState(externalId: string, state: SaveState): void {
    this.state.update((states) => new Map(states).set(externalId, state));
  }
}
