import {
  AfterViewInit,
  OnDestroy,
  Component,
  ElementRef,
  ViewChild,
  input,
  output,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { JobDto } from '../../../generated';

@Component({
  selector: 'app-job-detail',
  imports: [DatePipe],
  templateUrl: './job-detail.html',
  styleUrl: './job-detail.scss',
})
export class JobDetail implements AfterViewInit, OnDestroy {
  private destroyed = false;
  readonly job = input.required<JobDto>();
  readonly bookmarked = input(false);
  readonly bookmarkBusy = input(false);
  readonly bookmarkDisabled = input(false);
  readonly closeDisabled = input(false);
  readonly bookmarkToggle = output<void>();
  readonly closed = output<void>();

  @ViewChild('dialog', { static: true }) private dialog!: ElementRef<HTMLDialogElement>;

  ngAfterViewInit(): void {
    this.dialog.nativeElement.showModal();
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    if (this.dialog.nativeElement.open) this.dialog.nativeElement.close();
  }

  onClose(): void {
    if (!this.destroyed) this.closed.emit();
  }
}
