export interface CategoryProgress {
  kategorie: string;
  gesamt: number;
  abgeschlossen: number;
}

export interface CompletedContent {
  contentId: number;
  titel: string;
  kategorie: string;
  abgeschlossenAm: string;
}

export interface Progress {
  patientId: string;
  vorname: string;
  nachname: string;
  gesamt: number;
  abgeschlossen: number;
  kategorien: CategoryProgress[];
  abgeschlosseneInhalte: CompletedContent[];
}

const UEBUNGEN_KATEGORIE = 'Uebungen';

/** Uebungen gelten als "durchgefuehrt", alle anderen Tipps als "ausprobiert". */
export function completionLabel(kategorieName: string, done: boolean): string {
  const isUebung = kategorieName === UEBUNGEN_KATEGORIE;
  if (done) {
    return isUebung ? 'Durchgeführt' : 'Ausprobiert';
  }
  return isUebung ? 'Als durchgeführt markieren' : 'Als ausprobiert markieren';
}

export function progressPercent(progress: { gesamt: number; abgeschlossen: number }): number {
  return progress.gesamt === 0 ? 0 : Math.round((progress.abgeschlossen / progress.gesamt) * 100);
}
