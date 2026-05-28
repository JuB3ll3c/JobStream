import { Component, output, input, ChangeDetectionStrategy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { POPULAR_SEARCHES } from '../../../shared/utils/job.constants';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SearchBarComponent implements OnInit {
  // Inputs
  searchQuery = input<string>('');
  locationQuery = input<string>('');
  isLoading = input<boolean>(false);

  // Outputs
  searchSubmitted = output<{ query: string; location: string }>();
  popularSearchClicked = output<string>();

  // Form
  protected searchForm!: FormGroup;
  protected readonly popularSearches = POPULAR_SEARCHES;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      query: [this.searchQuery(), [Validators.required, Validators.minLength(2)]],
      location: [this.locationQuery(), []]
    });
  }

  onSearchSubmit(): void {
    if (this.searchForm.valid) {
      const { query, location } = this.searchForm.value;
      this.searchSubmitted.emit({ 
        query: query.trim(), 
        location: location?.trim() || '' 
      });
    } else {
      // Mark all fields as touched to show validation errors
      this.searchForm.markAllAsTouched();
    }
  }

  onPopularSearchClick(term: string): void {
    this.searchForm.patchValue({ query: term });
    this.popularSearchClicked.emit(term);
    this.onSearchSubmit();
  }

  clearSearch(): void {
    this.searchForm.patchValue({ query: '' });
  }

  clearLocation(): void {
    this.searchForm.patchValue({ location: '' });
  }

  get query() {
    return this.searchForm.get('query');
  }

  get location() {
    return this.searchForm.get('location');
  }
}
