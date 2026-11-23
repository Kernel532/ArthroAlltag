import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../../../core/services/auth';

/** Rahmen fuer den Therapeuten-Bereich: Begruessung, Navigation und die jeweils aktive Seite. */
@Component({
  selector: 'app-therapeut-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './therapeut-layout.html'
})
export class TherapeutLayout {
  protected readonly authService = inject(AuthService);
}
