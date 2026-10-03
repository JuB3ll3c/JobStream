import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EMPTY, Subject, catchError, map, merge, switchMap, tap } from 'rxjs';
import { JobOfferSearchResponse, JobOfferService } from '../../../generated';

@Component({
  selector: 'app-jobs',
  imports: [ReactiveFormsModule],
  templateUrl: './jobs.html',
  styleUrl: './jobs.scss',
})
export class Jobs {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(JobOfferService);
  private readonly router = inject(Router);
  private readonly retry = new Subject<void>();
  readonly form = new FormGroup({
    title: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/)],
    }),
    location: new FormControl('', { nonNullable: true }),
  });
  readonly results = signal<JobOfferSearchResponse | null>(null);
  readonly validationError = signal(false);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  constructor() {
    merge(this.route.queryParamMap, this.retry.pipe(map(() => this.route.snapshot.queryParamMap)))
      .pipe(
        switchMap((params) => {
          const title = (params.get('title') ?? '').trim();
          const location = (params.get('location') ?? '').trim();
          this.form.setValue({ title, location });
          this.results.set(null);
          this.loading.set(false);
          this.error.set(null);
          this.validationError.set(false);
          const page = Number(params.get('page') ?? 1);
          const size = Number(params.get('size') ?? 20);
          if (
            !Number.isSafeInteger(page) ||
            page < 1 ||
            !Number.isSafeInteger(size) ||
            size < 1 ||
            size > 100
          ) {
            this.error.set('Les paramètres de pagination sont invalides. Relancez la recherche.');
            return EMPTY;
          }
          if (!title) {
            this.validationError.set(params.has('title'));
            return EMPTY;
          }
          this.loading.set(true);
          return this.api
            .searchJobOffers({ title, location: location || undefined, page, size })
            .pipe(
              tap(() => this.loading.set(false)),
              catchError((error: HttpErrorResponse) => {
                this.loading.set(false);
                if (error.status === 400) {
                  this.error.set(
                    'Les critères de recherche sont invalides. Vérifiez le formulaire.',
                  );
                  return EMPTY;
                }
                this.error.set(
                  'La recherche est temporairement indisponible. Réessayez plus tard.',
                );
                return EMPTY;
              }),
            );
        }),
        takeUntilDestroyed(),
      )
      .subscribe((response) => this.results.set(response));
  }

  search(): void {
    if (this.loading()) return;
    const { title, location } = this.form.getRawValue();
    this.validationError.set(!title.trim());
    if (!title.trim()) {
      this.form.markAllAsTouched();
      return;
    }
    const target = this.router.createUrlTree([], {
      relativeTo: this.route,
      queryParams: { title: title.trim(), location: location.trim() || null, page: 1, size: 20 },
    });
    if (this.router.serializeUrl(target) === this.router.url) {
      this.retry.next();
    } else {
      void this.router.navigateByUrl(target);
    }
  }

  changePage(page: number): void {
    const results = this.results();
    if (!results || page < 1 || page > results.totalPages) return;
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page },
      queryParamsHandling: 'merge',
    });
  }
}
