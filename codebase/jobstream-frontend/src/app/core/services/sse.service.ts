import { Injectable } from '@angular/core';
import { Observable, Subject, fromEvent, merge } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SseService {
  private eventSource: EventSource | null = null;
  private analysisSubject = new Subject<any>();

  connect(): void {
    if (this.eventSource) {
      return;
    }

    this.eventSource = new EventSource('/api/events');

    this.eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        this.analysisSubject.next(data);
      } catch (e) {
        console.error('Failed to parse SSE message', e);
      }
    };

    this.eventSource.onerror = (error) => {
      console.error('SSE error', error);
      this.disconnect();
    };
  }

  disconnect(): void {
    this.eventSource?.close();
    this.eventSource = null;
  }

  getAnalysisUpdates(): Observable<any> {
    return this.analysisSubject.asObservable();
  }
}
