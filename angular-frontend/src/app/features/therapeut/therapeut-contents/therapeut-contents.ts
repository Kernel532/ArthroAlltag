import { Component, inject, signal } from '@angular/core';

import { AuthService } from '../../../core/services/auth';
import { CategoryService } from '../../../core/services/category';
import { ContentService } from '../../../core/services/content';
import { ModulService } from '../../../core/services/modul';
import { Category } from '../../../shared/models/category.model';
import { Content, ContentCreateRequest } from '../../../shared/models/content.model';
import { ErrorResponse } from '../../../shared/models/error-response.model';
import { Modul } from '../../../shared/models/modul.model';
import { CategoryFilter } from '../../../shared/components/category-filter/category-filter';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { Pagination } from '../../../shared/components/pagination/pagination';
import { StarRating } from '../../../shared/components/star-rating/star-rating';
import { ContentForm } from '../content-form/content-form';

const PAGE_SIZE = 10;

/** Verwaltung der Tipps und Uebungen: auflisten, filtern, anlegen, bearbeiten, loeschen. */
@Component({
  selector: 'app-therapeut-contents',
  imports: [ContentForm, CategoryFilter, ErrorMessage, LoadingSpinner, Pagination, StarRating],
  templateUrl: './therapeut-contents.html'
})
export class TherapeutContents {
  private readonly authService = inject(AuthService);
  private readonly categoryService = inject(CategoryService);
  private readonly contentService = inject(ContentService);
  private readonly modulService = inject(ModulService);

  protected readonly modules = signal<Modul[]>([]);
  protected readonly categories = signal<Category[]>([]);
  protected readonly contents = signal<Content[]>([]);
  protected readonly selectedCategoryId = signal<number | null>(null);
  protected readonly currentPage = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly totalElements = signal(0);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly showForm = signal(false);
  // null = neuen Tipp anlegen, Content = bestehenden bearbeiten - nur relevant, solange showForm true ist.
  protected readonly editingContent = signal<Content | null>(null);
  protected readonly isSaving = signal(false);
  protected readonly formErrorMessage = signal<string | null>(null);

  constructor() {
    this.categoryService.getCategories().subscribe({
      next: (categories) => this.categories.set(categories),
      error: () => this.errorMessage.set('Kategorien konnten nicht geladen werden.')
    });
    this.modulService.getAllModules().subscribe({
      next: (modules) => this.modules.set(modules),
      error: () => this.errorMessage.set('Module konnten nicht geladen werden.')
    });
    this.loadContents();
  }

  protected selectCategory(categoryId: number | null): void {
    if (categoryId === this.selectedCategoryId()) {
      return;
    }
    this.selectedCategoryId.set(categoryId);
    this.goToPage(0);
  }

  protected goToPage(page: number): void {
    this.currentPage.set(page);
    this.loadContents();
  }

  protected openCreateForm(): void {
    this.formErrorMessage.set(null);
    this.editingContent.set(null);
    this.showForm.set(true);
  }

  protected openEditForm(content: Content): void {
    this.formErrorMessage.set(null);
    this.editingContent.set(content);
    this.showForm.set(true);
  }

  protected closeForm(): void {
    this.showForm.set(false);
  }

  protected onSave(request: ContentCreateRequest): void {
    const therapeutId = this.authService.currentUser()?.userId;
    if (!therapeutId) {
      return;
    }

    const editing = this.editingContent();

    this.isSaving.set(true);
    this.formErrorMessage.set(null);

    const request$ =
      editing === null
        ? this.contentService.createContent(therapeutId, request)
        : this.contentService.updateContent(therapeutId, editing.id, request);

    request$.subscribe({
      next: () => {
        this.isSaving.set(false);
        this.closeForm();
        this.loadContents();
      },
      error: (error: { error?: ErrorResponse }) => {
        this.isSaving.set(false);
        this.formErrorMessage.set(error.error?.message ?? 'Tipp konnte nicht gespeichert werden.');
      }
    });
  }

  protected onDelete(content: Content): void {
    const therapeutId = this.authService.currentUser()?.userId;
    if (!therapeutId || !confirm(`"${content.titel}" wirklich loeschen?`)) {
      return;
    }

    this.contentService.deleteContent(therapeutId, content.id).subscribe({
      next: () => this.loadContents(),
      error: () => this.errorMessage.set('Tipp konnte nicht geloescht werden.')
    });
  }

  private loadContents(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.contentService.getAllContents(this.selectedCategoryId(), this.currentPage(), PAGE_SIZE).subscribe({
      next: (result) => {
        // Letzte Seite wurde z.B. durch Loeschen leer - eine Seite zurueckspringen.
        if (result.content.length === 0 && result.totalPages > 0 && this.currentPage() >= result.totalPages) {
          this.goToPage(result.totalPages - 1);
          return;
        }
        this.contents.set(result.content);
        this.totalPages.set(result.totalPages);
        this.totalElements.set(result.totalElements);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Tipps konnten nicht geladen werden.');
        this.isLoading.set(false);
      }
    });
  }
}
