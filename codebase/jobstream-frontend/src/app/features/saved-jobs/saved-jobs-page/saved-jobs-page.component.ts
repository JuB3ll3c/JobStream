import { Component, signal, inject, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { JobService } from '../../../core/services/job.service';
import { SavedJobDto } from '../../../core/models';
import { LoggerService } from '../../../core/services/logger.service';
import { ErrorHandlerService } from '../../../core/services/error-handler.service';
import { SseService } from '../../../core/services/sse.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { Subject, takeUntil } from 'rxjs';

interface Toast {
  message: string;
  type: 'success' | 'error';
}

@Component({
  selector: 'app-saved-jobs-page',
  standalone: true,
  imports: [CommonModule, RouterLink, ConfirmDialogComponent],
  templateUrl: './saved-jobs-page.component.html',
  styleUrl: './saved-jobs-page.component.css'
})
export class SavedJobsPageComponent implements OnInit, OnDestroy {
  @ViewChild('confirmDialog') confirmDialog!: ConfirmDialogComponent;

  private readonly jobService = inject(JobService);
  private readonly logger = inject(LoggerService);
  private readonly errorHandler = inject(ErrorHandlerService);
  private readonly sseService = inject(SseService);
  private readonly destroy$ = new Subject<void>();

  // State
  protected readonly savedJobs = signal<SavedJobDto[]>([]);
  protected readonly totalCount = signal<number>(0);
  protected readonly isLoading = signal<boolean>(false);
  protected readonly errorMessage = signal<string | null>(null);
  
  // Delete confirmation state
  protected jobToDelete = signal<SavedJobDto | null>(null);
  
  // Toast
  protected readonly toast = signal<Toast | null>(null);

  ngOnInit(): void {
    this.loadSavedJobs();
    this.connectToSse();
  }

  ngOnDestroy(): void {
    this.sseService.disconnect();
    this.destroy$.next();
    this.destroy$.complete();
  }

  private connectToSse(): void {
    this.sseService.connect();
    this.sseService.getAnalysisUpdates()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (analysis) => {
          // Update the saved job with the new analysis
          this.savedJobs.update(jobs => 
            jobs.map(job => {
              // Match by savedJobId - need to find the job by externalId or create relation
              if (job.id === analysis.savedJobId || job.externalId === analysis.jobId) {
                return {
                  ...job,
                  analysis: {
                    id: analysis.id || crypto.randomUUID(),
                    savedJobId: job.id,
                    status: 'COMPLETED' as any, // Hardcoded since we don't have status in the result
                    score: analysis.score,
                    summary: analysis.summary,
                    strengths: analysis.strengths || [],
                    weaknesses: analysis.weaknesses || [],
                    recommendations: analysis.recommendations || [],
                    tags: analysis.tags || [],
                    createdAt: analysis.createdAt,
                    completedAt: analysis.completedAt
                  }
                };
              }
              return job;
            })
          );
          this.showToast('Analyse terminée !', 'success');
          this.logger.info('Analysis received via SSE', { analysis });
        },
        error: (error) => {
          this.logger.error('SSE error', error);
        }
      });
  }

  protected loadSavedJobs(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.jobService.getSavedJobs(0, 100, 'savedAt,desc').subscribe({
      next: (response) => {
        this.savedJobs.set(response.content || []);
        this.totalCount.set(response.totalElements || 0);
        this.isLoading.set(false);
        
        this.logger.info('Saved jobs loaded', {
          count: response.content?.length || 0,
          total: response.totalElements || 0
        });
      },
      error: (error: Error) => {
        this.errorMessage.set(this.errorHandler.handleGenericError(error));
        this.isLoading.set(false);
        
        this.logger.error('Failed to load saved jobs', {
          error: error.message
        });
      }
    });
  }

  protected onDelete(savedJob: SavedJobDto): void {
    this.jobToDelete.set(savedJob);
    this.confirmDialog.data = {
      title: 'Supprimer l\'offre',
      message: `Voulez-vous vraiment supprimer "${savedJob.title}" de vos offres sauvegardées ?`,
      confirmText: 'Supprimer',
      cancelText: 'Annuler'
    };
    this.confirmDialog.open();
  }

  protected onConfirmDelete(): void {
    const job = this.jobToDelete();
    if (!job) return;

    this.jobService.deleteSavedJob(job.id).subscribe({
      next: () => {
        // Remove from list
        this.savedJobs.update(jobs => jobs.filter(j => j.id !== job.id));
        this.totalCount.update(count => count - 1);
        
        // Show toast
        this.showToast('Offre supprimée avec succès', 'success');
        
        this.logger.info('Job deleted', { id: job.id });
      },
      error: (error: Error) => {
        this.showToast('Erreur lors de la suppression', 'error');
        
        this.logger.error('Failed to delete job', {
          id: job.id,
          error: error.message
        });
      }
    });
  }

  protected onCancelDelete(): void {
    this.jobToDelete.set(null);
  }

  private showToast(message: string, type: 'success' | 'error'): void {
    this.toast.set({ message, type });
    setTimeout(() => {
      this.toast.set(null);
    }, 3000);
  }

  protected onAnalyze(savedJob: SavedJobDto): void {
    this.jobService.triggerAnalysis(savedJob.id).subscribe({
      next: () => {
        this.showToast('Analyse démarrée', 'success');
        this.logger.info('Analysis triggered', { id: savedJob.id });
      },
      error: (error: Error) => {
        this.showToast('Erreur lors du déclenchement de l\'analyse', 'error');
        this.logger.error('Failed to trigger analysis', { id: savedJob.id, error: error.message });
      }
    });
  }

  protected trackBySavedJob(index: number, job: SavedJobDto): string {
    return job.id;
  }

  protected getScoreValue(score: any): number | null {
    // Handle null/undefined
    if (score === null || score === undefined) return null;
    
    // Handle JsonNullable format {present: true, value: X}
    if (typeof score === 'object' && 'present' in score) {
      return score.present ? score.value : null;
    }
    
    // Handle plain number
    if (typeof score === 'number') return score;
    
    // Handle case where value might be in a 'value' property
    if (typeof score === 'object' && 'value' in score) {
      return score.value;
    }
    
    return null;
  }
}
