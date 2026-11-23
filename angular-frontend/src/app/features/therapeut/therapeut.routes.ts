import { Routes } from '@angular/router';

import { ownResourceGuard } from '../../core/guards/own-resource-guard';

export const therapeutRoutes: Routes = [
  {
    path: ':therapeutId',
    canActivate: [ownResourceGuard('THERAPEUT', 'therapeutId')],
    loadComponent: () => import('./therapeut-layout/therapeut-layout').then((module) => module.TherapeutLayout),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'inhalte' },
      {
        path: 'inhalte',
        loadComponent: () => import('./therapeut-contents/therapeut-contents').then((module) => module.TherapeutContents)
      },
      {
        path: 'patienten',
        loadComponent: () => import('./therapeut-patients/therapeut-patients').then((module) => module.TherapeutPatients)
      }
    ]
  }
];
