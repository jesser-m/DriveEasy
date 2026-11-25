import { Component, computed, input, output } from '@angular/core';

/** Pagination simple (pages numerotees a partir de 0). */
@Component({
  selector: 'app-pagination',
  template: `
    @if (totalPages() > 1) {
      <nav class="pagination">
        <button class="btn small" [disabled]="page() === 0" (click)="pageChange.emit(page() - 1)">&laquo; Precedent</button>
        @for (p of pages(); track p) {
          <button class="btn small" [class.primary]="p === page()" (click)="pageChange.emit(p)">{{ p + 1 }}</button>
        }
        <button class="btn small" [disabled]="page() >= totalPages() - 1" (click)="pageChange.emit(page() + 1)">Suivant &raquo;</button>
      </nav>
    }
  `,
})
export class Pagination {
  readonly page = input.required<number>();
  readonly totalPages = input.required<number>();
  readonly pageChange = output<number>();
  protected readonly pages = computed(() => Array.from({ length: this.totalPages() }, (_, i) => i));
}
