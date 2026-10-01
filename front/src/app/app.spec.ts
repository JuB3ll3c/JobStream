import { TestBed } from '@angular/core/testing';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should display the navbar', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const navbar: HTMLElement = fixture.nativeElement.querySelector('app-navbar');
    const routerOutlet: HTMLElement = fixture.nativeElement.querySelector('router-outlet');

    expect(navbar).not.toBeNull();
    expect(
      navbar.compareDocumentPosition(routerOutlet) & Node.DOCUMENT_POSITION_FOLLOWING,
    ).toBeTruthy();
  });
});
