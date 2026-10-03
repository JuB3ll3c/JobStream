import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, NavigationEnd, Router, RouterLink } from '@angular/router';
import {
  EMPTY,
  catchError,
  distinctUntilChanged,
  filter,
  map,
  of,
  startWith,
  switchMap,
  tap,
} from 'rxjs';
import { JobDto, JobOfferService } from '../../../generated';
import { JobSaving } from '../service/job-saving';

@Component({
  selector: 'app-job-detail',
  imports: [RouterLink, DatePipe],
  providers: [JobSaving],
  templateUrl: './job-detail.html',
  styleUrl: './job-detail.scss',
})
export class JobDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(JobOfferService);
  private readonly router = inject(Router);
  private readonly jobSaving = inject(JobSaving);
  readonly saveStates = this.jobSaving.states;
  readonly offer = signal<JobDto | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  get searchQueryParams() {
    return this.route.snapshot.queryParams;
  }

  save(offer: JobDto): void {
    this.jobSaving.save(offer);
  }

  constructor() {
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        startWith(null),
        map(() => ({
          snapshot: this.route.snapshot,
          selected: this.router.currentNavigation()?.extras.state?.['offer'] as JobDto | undefined,
        })),
        distinctUntilChanged(
          (previous, current) =>
            previous.snapshot.paramMap.get('externalId') ===
              current.snapshot.paramMap.get('externalId') &&
            JSON.stringify(previous.snapshot.queryParams) ===
              JSON.stringify(current.snapshot.queryParams),
        ),
        switchMap(({ snapshot, selected }) => {
          this.offer.set(null);
          this.error.set(null);
          this.loading.set(false);
          const params = snapshot.queryParamMap;
          const externalId = snapshot.paramMap.get('externalId');
          if (
            selected?.externalId === externalId &&
            typeof selected.title === 'string' &&
            typeof selected.company === 'string' &&
            typeof selected.location === 'string'
          ) {
            return of(selected);
          }
          const title = (params.get('title') ?? '').trim();
          const location = (params.get('location') ?? '').trim();
          const page = Number(params.get('page') ?? 1);
          const size = Number(params.get('size') ?? 20);
          if (!title) {
            this.error.set(
              'Ouvrez cette offre depuis une recherche pour retrouver ses informations.',
            );
            return EMPTY;
          }
          if (
            !Number.isSafeInteger(page) ||
            page < 1 ||
            !Number.isSafeInteger(size) ||
            size < 1 ||
            size > 100 ||
            title.length > 200 ||
            location.length > 200
          ) {
            this.error.set('Le contexte de recherche est invalide. Relancez la recherche.');
            return EMPTY;
          }
          this.loading.set(true);
          return this.api
            .searchJobOffers({ title, location: location || undefined, page, size })
            .pipe(
              map(
                (response) =>
                  response.content.find((offer) => offer.externalId === externalId) ?? null,
              ),
              tap((offer) => {
                this.loading.set(false);
                if (!offer)
                  this.error.set('Cette offre n’est plus disponible dans cette recherche.');
              }),
              catchError((error: HttpErrorResponse) => {
                this.loading.set(false);
                this.error.set(
                  error.status === 400
                    ? 'Le contexte de recherche est invalide. Relancez la recherche.'
                    : 'La recherche est temporairement indisponible. Réessayez plus tard.',
                );
                return EMPTY;
              }),
            );
        }),
        takeUntilDestroyed(),
      )
      .subscribe((offer) => this.offer.set(offer));
  }
}
