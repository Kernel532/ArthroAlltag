import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../api-base-url';
import { Page } from '../../shared/models/page.model';
import { Patient } from '../../shared/models/patient.model';

@Injectable({ providedIn: 'root' })
export class PatientService {
  private readonly http = inject(HttpClient);

  getPatients(therapeutId: string, page: number, size: number): Observable<Page<Patient>> {
    return this.http.get<Page<Patient>>(`${API_BASE_URL}/therapeuts/${therapeutId}/patients`, {
      params: new HttpParams().set('page', page).set('size', size)
    });
  }

  getUnassignedPatients(therapeutId: string, page: number, size: number): Observable<Page<Patient>> {
    return this.http.get<Page<Patient>>(`${API_BASE_URL}/therapeuts/${therapeutId}/patients/unassigned`, {
      params: new HttpParams().set('page', page).set('size', size)
    });
  }

  assignPatient(therapeutId: string, patientId: string): Observable<Patient> {
    return this.http.post<Patient>(
      `${API_BASE_URL}/therapeuts/${therapeutId}/patients/${patientId}/assignment`,
      null
    );
  }

  unassignPatient(therapeutId: string, patientId: string): Observable<void> {
    return this.http.delete<void>(
      `${API_BASE_URL}/therapeuts/${therapeutId}/patients/${patientId}/assignment`
    );
  }
}
