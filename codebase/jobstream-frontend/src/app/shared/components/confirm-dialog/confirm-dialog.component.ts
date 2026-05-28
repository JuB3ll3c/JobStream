import { Component, signal, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirm-dialog.component.html',
  styleUrl: './confirm-dialog.component.css'
})
export class ConfirmDialogComponent {
  @Input() data: ConfirmDialogData = {
    title: 'Confirmer',
    message: 'Êtes-vous sûr ?',
    confirmText: 'Confirmer',
    cancelText: 'Annuler'
  };

  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  protected isVisible = signal(false);

  open(): void {
    this.isVisible.set(true);
  }

  protected onConfirm(): void {
    this.isVisible.set(false);
    this.confirm.emit();
  }

  protected onCancel(): void {
    this.isVisible.set(false);
    this.cancel.emit();
  }

  protected onOverlayClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('dialog-overlay')) {
      this.onCancel();
    }
  }
}
