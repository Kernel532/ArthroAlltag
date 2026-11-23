import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth';
import { ErrorResponse } from '../../../shared/models/error-response.model';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';

const USERNAME_PATTERN = /^[A-Za-z0-9_]{3,10}$/;
const MIN_PASSWORD_LENGTH = 6;

@Component({
  selector: 'app-register',
  imports: [ErrorMessage, RouterLink],
  templateUrl: './register.html'
})
export class Register {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly vorname = signal('');
  protected readonly nachname = signal('');
  protected readonly username = signal('');
  protected readonly password = signal('');
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected onInput(field: 'vorname' | 'nachname' | 'username' | 'password', event: Event): void {
    this[field].set((event.target as HTMLInputElement).value);
  }

  protected onSubmit(): void {
    const validationError = this.validate();
    if (validationError) {
      this.errorMessage.set(validationError);
      return;
    }

    this.errorMessage.set(null);
    this.isLoading.set(true);

    this.authService
      .register({
        vorname: this.vorname().trim(),
        nachname: this.nachname().trim(),
        username: this.username().trim(),
        password: this.password()
      })
      .subscribe({
        next: (response) => {
          this.isLoading.set(false);
          this.router.navigateByUrl(`/patient/${response.userId}`);
        },
        error: (error: { error?: ErrorResponse }) => {
          this.isLoading.set(false);
          this.errorMessage.set(error.error?.message ?? 'Registrierung fehlgeschlagen.');
        }
      });
  }

  private validate(): string | null {
    if (!this.vorname().trim() || !this.nachname().trim()) {
      return 'Bitte Vor- und Nachnamen angeben.';
    }
    if (!USERNAME_PATTERN.test(this.username().trim())) {
      return 'Der Benutzername muss 3-10 Zeichen lang sein (Buchstaben, Ziffern, _).';
    }
    if (this.password().length < MIN_PASSWORD_LENGTH) {
      return `Das Passwort muss mindestens ${MIN_PASSWORD_LENGTH} Zeichen lang sein.`;
    }
    return null;
  }
}
