import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../api-base-url';
import { Content, ContentCreateRequest } from '../../shared/models/content.model';
import { Page } from '../../shared/models/page.model';

@Injectable({ providedIn: 'root' })
export class ContentService {
  private readonly http = inject(HttpClient);

  getContents(
    patientId: string,
    options: { categoryId: number | null; search: string; favoritesOnly: boolean; page: number; size: number }
  ): Observable<Page<Content>> {
    let params = this.categoryParams(options.categoryId)
      .set('favoritesOnly', options.favoritesOnly)
      .set('page', options.page)
      .set('size', options.size);
    if (options.search.trim()) {
      params = params.set('search', options.search.trim());
    }
    return this.http.get<Page<Content>>(`${API_BASE_URL}/patients/${patientId}/contents`, { params });
  }

  // Rollenunabhaengiger Lesezugriff (/api/contents) - genutzt von der Therapeuten-Verwaltung,
  // die alle Tipps sehen muss und nicht nur die eines einzelnen Patienten. Seitenweise.
  getAllContents(categoryId: number | null, page: number, size: number): Observable<Page<Content>> {
    return this.http.get<Page<Content>>(`${API_BASE_URL}/contents`, {
      params: this.categoryParams(categoryId).set('page', page).set('size', size)
    });
  }

  createContent(therapeutId: string, request: ContentCreateRequest): Observable<Content> {
    return this.http.post<Content>(`${API_BASE_URL}/therapeuts/${therapeutId}/contents`, request);
  }

  updateContent(therapeutId: string, contentId: number, request: ContentCreateRequest): Observable<Content> {
    return this.http.put<Content>(`${API_BASE_URL}/therapeuts/${therapeutId}/contents/${contentId}`, request);
  }

  deleteContent(therapeutId: string, contentId: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/therapeuts/${therapeutId}/contents/${contentId}`);
  }

  private categoryParams(categoryId?: number | null): HttpParams {
    let params = new HttpParams();
    if (categoryId != null) {
      params = params.set('categoryId', categoryId);
    }
    return params;
  }
}
