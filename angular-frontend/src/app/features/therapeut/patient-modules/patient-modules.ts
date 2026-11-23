import { Component, inject, input, OnInit, signal } from '@angular/core';

import { AuthService } from '../../../core/services/auth';
import { ModulService } from '../../../core/services/modul';
import { ErrorResponse } from '../../../shared/models/error-response.model';
import { Modul } from '../../../shared/models/modul.model';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';

/** Module eines einzelnen Patienten freischalten bzw. zuruecksetzen. */
@Component({
  selector: 'app-patient-modules',
  imports: [ErrorMessage, LoadingSpinner],
  templateUrl: './patient-modules.html'
})
export class PatientModules implements OnInit {
  readonly patientId = input.required<string>();
  readonly modules = input.required<Modul[]>();

  private readonly authService = inject(AuthService);
  private readonly modulService = inject(ModulService);

  protected readonly unlockedModuleIds = signal<Set<number>>(new Set());
  protected readonly isLoading = signal(true);
  protected readonly unlockingModuleId = signal<number | null>(null);
  protected readonly revokingModuleId = signal<number | null>(null);
  protected readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadUnlocked();
  }

  protected isUnlocked(modulId: number): boolean {
    return this.unlockedModuleIds().has(modulId);
  }

  protected unlock(modul: Modul): void {
    const therapeutId = this.authService.currentUser()?.userId;
    if (!therapeutId) {
      return;
    }

    this.unlockingModuleId.set(modul.id);
    this.errorMessage.set(null);

    this.modulService.unlockModule(therapeutId, { patientId: this.patientId(), modulId: modul.id }).subscribe({
      next: () => {
        this.unlockedModuleIds.update((ids) => new Set(ids).add(modul.id));
        this.unlockingModuleId.set(null);
      },
      error: (error: { error?: ErrorResponse }) => {
        this.errorMessage.set(error.error?.message ?? 'Modul konnte nicht freigeschaltet werden.');
        this.unlockingModuleId.set(null);
      }
    });
  }

  protected reset(modul: Modul): void {
    const therapeutId = this.authService.currentUser()?.userId;
    if (!therapeutId || !confirm(`"${modul.name}" wirklich zuruecksetzen? Der Patient sieht das Modul danach nicht mehr.`)) {
      return;
    }

    this.revokingModuleId.set(modul.id);
    this.errorMessage.set(null);

    this.modulService.revokeModule(therapeutId, this.patientId(), modul.id).subscribe({
      next: () => {
        this.unlockedModuleIds.update((ids) => {
          const updated = new Set(ids);
          updated.delete(modul.id);
          return updated;
        });
        this.revokingModuleId.set(null);
      },
      error: (error: { error?: ErrorResponse }) => {
        this.errorMessage.set(error.error?.message ?? 'Modul konnte nicht zurueckgesetzt werden.');
        this.revokingModuleId.set(null);
      }
    });
  }

  private loadUnlocked(): void {
    const therapeutId = this.authService.currentUser()?.userId;
    if (!therapeutId) {
      return;
    }

    this.modulService.getPatientModules(therapeutId, this.patientId()).subscribe({
      next: (modules) => {
        this.unlockedModuleIds.set(new Set(modules.map((modul) => modul.id)));
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Freischaltstatus konnte nicht geladen werden.');
        this.isLoading.set(false);
      }
    });
  }
}
