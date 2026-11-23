import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../api-base-url';
import { Page } from '../../shared/models/page.model';
import { Progress } from '../../shared/models/progress.model';

@Injectable({ providedIn: 'root' })
export class ProgressService {
  private readonly http = inject(HttpClient);

  getProgress(patientId: string): Observable<Progress> {
    return this.http.get<Progress>(`${API_BASE_URL}/patients/${patientId}/progress`);
  }

  markCompleted(patientId: string, contentId: number): Observable<void> {
    return this.http.put<void>(`${API_BASE_URL}/patients/${patientId}/completions/${contentId}`, null);
  }

  unmarkCompleted(patientId: string, contentId: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/patients/${patientId}/completions/${contentId}`);
  }

  // Gleiche page/size wie die Patientenliste, damit Fortschritt und Patienten zusammenpassen.
  getPatientsProgress(therapeutId: string, page: number, size: number): Observable<Page<Progress>> {
    return this.http.get<Page<Progress>>(`${API_BASE_URL}/therapeuts/${therapeutId}/patients/progress`, {
      params: new HttpParams().set('page', page).set('size', size)
    });
  }
}
