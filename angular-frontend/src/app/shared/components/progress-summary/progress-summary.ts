import { Component, input } from '@angular/core';

import { Progress, progressPercent } from '../../models/progress.model';

/** Fortschrittsbalken mit Gesamtanzahl und Aufschluesselung nach Kategorie. */
@Component({
  selector: 'app-progress-summary',
  templateUrl: './progress-summary.html'
})
export class ProgressSummary {
  readonly progress = input.required<Progress>();

  protected percent(value: { gesamt: number; abgeschlossen: number }): number {
    return progressPercent(value);
  }
}
