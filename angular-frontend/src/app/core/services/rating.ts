import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../api-base-url';
import { Rating } from '../../shared/models/rating.model';

@Injectable({ providedIn: 'root' })
export class RatingService {
  private readonly http = inject(HttpClient);

  getRatings(patientId: string): Observable<Rating[]> {
    return this.http.get<Rating[]>(`${API_BASE_URL}/patients/${patientId}/ratings`);
  }

  rate(patientId: string, contentId: number, sterne: number): Observable<Rating> {
    return this.http.put<Rating>(`${API_BASE_URL}/patients/${patientId}/ratings/${contentId}`, {
      sterne
    });
  }
}
