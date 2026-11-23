import { Category } from './category.model';
import { Modul } from './modul.model';

export type Schwierigkeitsgrad = 'LEICHT' | 'MITTEL' | 'SCHWER';

export interface Content {
  id: number;
  titel: string;
  beschreibung: string;
  kategorie: Category;
  modul: Modul | null;
  bildUrl: string | null;
  schwierigkeitsgrad: Schwierigkeitsgrad | null;
  hilfsmittel: string | null;
  erstelltDurchName: string;
  erstelltAm: string;
  aktualisiertAm: string;
  durchschnittsBewertung: number | null;
  anzahlBewertungen: number;
}

export interface ContentCreateRequest {
  titel: string;
  beschreibung: string;
  kategorieId: number;
  modulId: number;
  bildUrl?: string | null;
  schwierigkeitsgrad?: Schwierigkeitsgrad | null;
  hilfsmittel?: string | null;
}
