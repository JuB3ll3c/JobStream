import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { JobService } from '../../../core/services/job.service';
import { LoggerService } from '../../../core/services/logger.service';

interface JobAnalysisResult {
  jobId: string;
  score: number | null;
  summary: string | null;
  strengths: string[] | null;
  weaknesses: string[] | null;
  recommendations: string[] | null;
  tags: string[] | null;
  createdAt: string | null;
  completedAt: string | null;
  errorMessage: string | null;
}

@Component({
  selector: 'app-analysis-result',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './analysis-result.component.html',
  styleUrls: ['./analysis-result.component.css']
})
export class AnalysisResultComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private jobService = inject(JobService);
  private logger = inject(LoggerService);

  savedJobId: string = '';
  analysis: JobAnalysisResult | null = null;
  loading = true;
  error: string | null = null;

  ngOnInit(): void {
    this.savedJobId = this.route.snapshot.paramMap.get('id') || '';
    if (this.savedJobId) {
      this.loadAnalysis();
    } else {
      this.error = 'ID de job invalide';
      this.loading = false;
    }
  }

  loadAnalysis(): void {
    this.loading = true;
    this.error = null;

    this.jobService.getAnalysisBySavedJobId(this.savedJobId).subscribe({
      next: (data) => {
        this.analysis = data;
        this.loading = false;
      },
      error: (err) => {
        this.logger.error('Failed to load analysis', err);
        this.error = 'Impossible de charger les résultats de l\'analyse';
        this.loading = false;
      }
    });
  }

  getScoreColor(score: number | null): string {
    if (score === null) return '#ccc';
    if (score >= 80) return '#22c55e'; // green
    if (score >= 60) return '#f59e0b'; // orange
    return '#ef4444'; // red
  }

  getScoreLabel(score: number | null): string {
    if (score === null) return 'N/A';
    if (score >= 80) return 'Excellent';
    if (score >= 60) return 'Bon';
    if (score >= 40) return 'Moyen';
    return 'Faible';
  }

  formatDate(dateStr: string | null): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('fr-CH', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}