import { Component, inject, signal } from '@angular/core';

import { AuthService } from '../../../core/services/auth';
import { ModulService } from '../../../core/services/modul';
import { PatientService } from '../../../core/services/patient';
import { ProgressService } from '../../../core/services/progress';
import { ErrorResponse } from '../../../shared/models/error-response.model';
import { Modul } from '../../../shared/models/modul.model';
import { Patient } from '../../../shared/models/patient.model';
import { Progress } from '../../../shared/models/progress.model';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { Pagination } from '../../../shared/components/pagination/pagination';
import { ProgressSummary } from '../../../shared/components/progress-summary/progress-summary';
import { PatientModules } from '../patient-modules/patient-modules';

const PAGE_SIZE = 10;

/** Therapeuten-Zuordnung (uebernehmen/abgeben) und Fortschrittsuebersicht der eigenen Patienten. */
@Component({
  selector: 'app-therapeut-patients',
  imports: [ErrorMessage, Pagination, ProgressSummary, PatientModules],
  templateUrl: './therapeut-patients.html'
})
export class TherapeutPatients {
  private readonly authService = inject(AuthService);
  private readonly patientService = inject(PatientService);
  private readonly progressService = inject(ProgressService);
  private readonly modulService = inject(ModulService);

  protected readonly modules = signal<Modul[]>([]);
  // Patient, dessen Modul-Verwaltung gerade aufgeklappt ist.
  protected readonly expandedPatientId = signal<string | null>(null);
  protected readonly patients = signal<Patient[]>([]);
  protected readonly unassignedPatients = signal<Patient[]>([]);
  protected readonly progressByPatientId = signal<Map<string, Progress>>(new Map());
  protected readonly minePage = signal(0);
  protected readonly mineTotalPages = signal(0);
  protected readonly mineTotalElements = signal(0);
  protected readonly unassignedPage = signal(0);
  protected readonly unassignedTotalPages = signal(0);
  protected readonly unassignedTotalElements = signal(0);
  protected readonly assigningPatientId = signal<string | null>(null);
  protected readonly errorMessage = signal<string | null>(null);

  constructor() {
    this.loadPatients();
    this.modulService.getAllModules().subscribe({
      next: (modules) => this.modules.set(modules),
      error: () => this.errorMessage.set('Module konnten nicht geladen werden.')
    });
  }

  protected toggleModules(patient: Patient): void {
    this.expandedPatientId.update((id) => (id === patient.id ? null : patient.id));
  }

  protected assignPatient(patient: Patient): void {
    const therapeutId = this.authService.currentUser()?.userId;
    if (!therapeutId) {
      return;
    }

    this.assigningPatientId.set(patient.id);
    this.errorMessage.set(null);

    this.patientService.assignPatient(therapeutId, patient.id).subscribe({
      next: () => {
        this.assigningPatientId.set(null);
        this.loadPatients();
      },
      error: (error: { error?: ErrorResponse }) => {
        this.errorMessage.set(error.error?.message ?? 'Patient konnte nicht uebernommen werden.');
        this.assigningPatientId.set(null);
        this.loadPatients();
      }
    });
  }

  protected releasePatient(patient: Patient): void {
    const therapeutId = this.authService.currentUser()?.userId;
    if (!therapeutId || !confirm(`${patient.vorname} ${patient.nachname} wirklich abgeben?`)) {
      return;
    }

    this.assigningPatientId.set(patient.id);
    this.errorMessage.set(null);

    this.patientService.unassignPatient(therapeutId, patient.id).subscribe({
      next: () => {
        this.assigningPatientId.set(null);
        this.loadPatients();
      },
      error: (error: { error?: ErrorResponse }) => {
        this.errorMessage.set(error.error?.message ?? 'Patient konnte nicht abgegeben werden.');
        this.assigningPatientId.set(null);
      }
    });
  }

  protected goToMinePage(page: number): void {
    this.minePage.set(page);
    this.loadMine();
  }

  protected goToUnassignedPage(page: number): void {
    this.unassignedPage.set(page);
    this.loadUnassigned();
  }

  private loadPatients(): void {
    this.loadMine();
    this.loadUnassigned();
  }

  private loadMine(): void {
    const therapeutId = this.authService.currentUser()?.userId;
    if (!therapeutId) {
      return;
    }

    this.patientService.getPatients(therapeutId, this.minePage(), PAGE_SIZE).subscribe({
      next: (result) => {
        // Letzte Seite wurde durch Abgeben leer - eine Seite zurueckspringen.
        if (result.content.length === 0 && result.totalPages > 0 && this.minePage() >= result.totalPages) {
          this.goToMinePage(result.totalPages - 1);
          return;
        }
        this.patients.set(result.content);
        this.mineTotalPages.set(result.totalPages);
        this.mineTotalElements.set(result.totalElements);
      },
      error: () => this.errorMessage.set('Patienten konnten nicht geladen werden.')
    });
    this.progressService.getPatientsProgress(therapeutId, this.minePage(), PAGE_SIZE).subscribe({
      next: (result) =>
        this.progressByPatientId.set(new Map(result.content.map((progress) => [progress.patientId, progress]))),
      error: () => this.errorMessage.set('Fortschritt konnte nicht geladen werden.')
    });
  }

  private loadUnassigned(): void {
    const therapeutId = this.authService.currentUser()?.userId;
    if (!therapeutId) {
      return;
    }

    this.patientService.getUnassignedPatients(therapeutId, this.unassignedPage(), PAGE_SIZE).subscribe({
      next: (result) => {
        if (result.content.length === 0 && result.totalPages > 0 && this.unassignedPage() >= result.totalPages) {
          this.goToUnassignedPage(result.totalPages - 1);
          return;
        }
        this.unassignedPatients.set(result.content);
        this.unassignedTotalPages.set(result.totalPages);
        this.unassignedTotalElements.set(result.totalElements);
      },
      error: () => this.errorMessage.set('Patienten konnten nicht geladen werden.')
    });
  }
}
