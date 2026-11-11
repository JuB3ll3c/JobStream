import { Component, input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { JobDto } from '../../../generated';

@Component({
  selector: 'app-job-detail',
  imports: [DatePipe],
  templateUrl: './job-detail.html',
  styleUrl: './job-detail.scss',
})
export class JobDetail {
  readonly job = input.required<JobDto>();
}
