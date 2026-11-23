import { Component, computed, input, output } from '@angular/core';

/** Blaetter-Leiste fuer seitenweise geladene Listen (Seitennummer 0-basiert wie im Backend). */
@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.html'
})
export class Pagination {
  readonly page = input.required<number>();
  readonly totalPages = input.required<number>();
  readonly totalElements = input.required<number>();
  readonly label = input('Seiten');

  readonly pageChange = output<number>();

  protected readonly isFirst = computed(() => this.page() <= 0);
  protected readonly isLast = computed(() => this.page() >= this.totalPages() - 1);

  protected go(page: number): void {
    if (page >= 0 && page < this.totalPages() && page !== this.page()) {
      this.pageChange.emit(page);
    }
  }
}
