import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Pagination } from './pagination';

describe('Pagination', () => {
  let fixture: ComponentFixture<Pagination>;
  let emitted: number[];

  function setup(page: number, totalPages: number): void {
    fixture = TestBed.createComponent(Pagination);
    fixture.componentRef.setInput('page', page);
    fixture.componentRef.setInput('totalPages', totalPages);
    fixture.componentRef.setInput('totalElements', totalPages * 10);
    emitted = [];
    fixture.componentInstance.pageChange.subscribe((value) => emitted.push(value));
    fixture.detectChanges();
  }

  function buttons(): HTMLButtonElement[] {
    return Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('button'));
  }

  it('blendet sich bei nur einer Seite aus', () => {
    setup(0, 1);

    expect(buttons()).toHaveLength(0);
  });

  it('sperrt Zurueck auf der ersten Seite und Weiter auf der letzten', () => {
    setup(0, 3);
    expect(buttons()[0].disabled).toBe(true);
    expect(buttons()[1].disabled).toBe(false);

    setup(2, 3);
    expect(buttons()[0].disabled).toBe(false);
    expect(buttons()[1].disabled).toBe(true);
  });

  it('meldet die neue 0-basierte Seite', () => {
    setup(1, 3);

    buttons()[1].click();
    buttons()[0].click();

    expect(emitted).toEqual([2, 0]);
  });
});
