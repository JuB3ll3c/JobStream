import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CvMetadataDto, CvService } from '../../generated';

@Component({
  selector: 'app-cv',
  imports: [DatePipe, DecimalPipe],
  templateUrl: './cv.html',
  styleUrl: './cv.scss',
})
export class Cv {
  private readonly api = inject(CvService);
  private readonly destroyRef = inject(DestroyRef);
  readonly metadata = signal<CvMetadataDto | null>(null);
  readonly loading = signal(false);
  readonly loadError = signal<string | null>(null);
  readonly selectedFile = signal<File | null>(null);
  readonly saving = signal(false);
  readonly uploadError = signal<string | null>(null);
  readonly success = signal(false);

  constructor() {
    this.load();
  }

  load(): void {
    if (this.loading()) return;
    this.loading.set(true);
    this.loadError.set(null);
    this.api
      .getCvMetadata()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (metadata) => {
          this.metadata.set(metadata);
          this.loading.set(false);
        },
        error: (error: HttpErrorResponse) => {
          this.loading.set(false);
          if (error.status === 404) {
            this.metadata.set(null);
          } else {
            this.loadError.set('Impossible de charger votre CV. Réessayez.');
          }
        },
      });
  }

  selectFile(input: HTMLInputElement): void {
    const file = input.files?.item(0) ?? null;
    this.selectedFile.set(null);
    this.uploadError.set(null);
    this.success.set(false);
    if (!file) return;
    if (file.size === 0) {
      this.uploadError.set('Le fichier est vide. Choisissez un PDF non vide.');
    } else if (file.size > 5_000_000) {
      this.uploadError.set('Le PDF ne doit pas dépasser 5 Mo.');
    } else if (file.type !== 'application/pdf') {
      this.uploadError.set('Choisissez un fichier au format PDF.');
    } else {
      this.selectedFile.set(file);
    }
    if (!this.selectedFile()) input.value = '';
  }

  upload(input: HTMLInputElement): void {
    const file = this.selectedFile();
    if (!file || this.saving() || this.loading() || this.loadError()) return;
    this.saving.set(true);
    this.uploadError.set(null);
    this.success.set(false);
    this.api
      .uploadCv({ file })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (metadata) => {
          this.metadata.set(metadata);
          this.selectedFile.set(null);
          input.value = '';
          this.saving.set(false);
          this.success.set(true);
        },
        error: (error: HttpErrorResponse) => {
          this.saving.set(false);
          switch (error.status) {
            case 400:
              this.uploadError.set(
                'Le PDF est invalide ou illisible. Choisissez un autre fichier.',
              );
              break;
            case 413:
              this.uploadError.set('Le PDF ne doit pas dépasser 5 Mo.');
              break;
            case 415:
              this.uploadError.set('Le fichier doit être un PDF. Choisissez un autre fichier.');
              break;
            default:
              this.uploadError.set('Impossible d’enregistrer votre CV. Réessayez.');
          }
        },
      });
  }
}
