import { Component, computed, inject, signal } from '@angular/core';

import { AuthService } from '../../../core/services/auth';
import { CategoryService } from '../../../core/services/category';
import { ContentService } from '../../../core/services/content';
import { FavoriteService } from '../../../core/services/favorite';
import { ModulService } from '../../../core/services/modul';
import { RatingService } from '../../../core/services/rating';
import { Category } from '../../../shared/models/category.model';
import { Content } from '../../../shared/models/content.model';
import { Favorite } from '../../../shared/models/favorite.model';
import { Modul } from '../../../shared/models/modul.model';
import { Rating } from '../../../shared/models/rating.model';
import { CategoryFilter } from '../../../shared/components/category-filter/category-filter';
import { ErrorMessage } from '../../../shared/components/error-message/error-message';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { StarRating } from '../../../shared/components/star-rating/star-rating';
import { ProgressSummary } from '../../../shared/components/progress-summary/progress-summary';
import { ProgressService } from '../../../core/services/progress';
import { Progress, completionLabel } from '../../../shared/models/progress.model';
import { Pagination } from '../../../shared/components/pagination/pagination';

const PAGE_SIZE = 10;

@Component({
  selector: 'app-patient-home',
  imports: [CategoryFilter, ErrorMessage, LoadingSpinner, StarRating, ProgressSummary, Pagination],
  templateUrl: './patient-home.html'
})
export class PatientHome {
  protected readonly authService = inject(AuthService);
  private readonly categoryService = inject(CategoryService);
  private readonly contentService = inject(ContentService);
  private readonly favoriteService = inject(FavoriteService);
  private readonly modulService = inject(ModulService);
  private readonly ratingService = inject(RatingService);
  private readonly progressService = inject(ProgressService);

  protected readonly progress = signal<Progress | null>(null);
  private readonly completedIds = computed(
    () => new Set((this.progress()?.abgeschlosseneInhalte ?? []).map((item) => item.contentId))
  );

  protected readonly categories = signal<Category[]>([]);
  protected readonly contents = signal<Content[]>([]);
  protected readonly ratings = signal<Rating[]>([]);
  protected readonly favorites = signal<Favorite[]>([]);
  protected readonly modules = signal<Modul[]>([]);
  protected readonly selectedCategoryId = signal<number | null>(null);
  protected readonly searchTerm = signal('');
  protected readonly showFavoritesOnly = signal(false);
  protected readonly isLoading = signal(true);
  protected readonly isLoadingModules = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  // Map von Content-Id auf Favorit-Id: fuer die Anzeige (ist ein Tipp favorisiert?) und
  // zum Entfernen (die DELETE-Route braucht die Favorit-Id, nicht die Content-Id).
  private readonly favoriteIdByContentId = computed(
    () => new Map(this.favorites().map((favorite) => [favorite.tippId, favorite.id]))
  );

  // Kategorie, Textsuche und Favoriten-Filter laufen im Backend, damit sie ueber alle Seiten
  // wirken (nicht nur ueber die aktuell geladene Seite).
  protected readonly filteredContents = this.contents;
  protected readonly currentPage = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly totalElements = signal(0);
  private searchTimer: ReturnType<typeof setTimeout> | undefined;

  constructor() {
    this.categoryService.getCategories().subscribe({
      next: (categories) => this.categories.set(categories),
      error: () => this.errorMessage.set('Kategorien konnten nicht geladen werden.')
    });
    this.loadFavorites();
    this.loadRatings();
    this.loadContents();
    this.loadModules();
    this.loadProgress();
  }

  protected isCompleted(contentId: number): boolean {
    return this.completedIds().has(contentId);
  }

  protected completionLabel(content: Content): string {
    return completionLabel(content.kategorie.name, this.isCompleted(content.id));
  }

  protected toggleCompleted(content: Content): void {
    const patientId = this.authService.currentUser()?.userId;
    if (!patientId) {
      return;
    }

    const request = this.isCompleted(content.id)
      ? this.progressService.unmarkCompleted(patientId, content.id)
      : this.progressService.markCompleted(patientId, content.id);

    request.subscribe({
      next: () => this.loadProgress(),
      error: () => this.errorMessage.set('Fortschritt konnte nicht gespeichert werden.')
    });
  }

  private loadProgress(): void {
    const patientId = this.authService.currentUser()?.userId;
    if (!patientId) {
      return;
    }

    this.progressService.getProgress(patientId).subscribe({
      next: (progress) => this.progress.set(progress),
      error: () => this.errorMessage.set('Fortschritt konnte nicht geladen werden.')
    });
  }

  protected onSearchInput(event: Event): void {
    this.searchTerm.set((event.target as HTMLInputElement).value);
    clearTimeout(this.searchTimer);
    this.searchTimer = setTimeout(() => this.reloadFromStart(), 300);
  }

  protected selectCategory(categoryId: number | null): void {
    if (categoryId === this.selectedCategoryId()) {
      return;
    }
    this.selectedCategoryId.set(categoryId);
    this.reloadFromStart();
  }

  protected toggleFavoritesOnly(): void {
    this.showFavoritesOnly.update((value) => !value);
    this.reloadFromStart();
  }

  protected goToPage(page: number): void {
    this.currentPage.set(page);
    this.loadContents();
  }

  private reloadFromStart(): void {
    this.currentPage.set(0);
    this.loadContents();
  }

  protected isFavorite(contentId: number): boolean {
    return this.favoriteIdByContentId().has(contentId);
  }

  protected toggleFavorite(content: Content): void {
    const patientId = this.authService.currentUser()?.userId;
    if (!patientId) {
      return;
    }

    const favoriteId = this.favoriteIdByContentId().get(content.id);

    if (favoriteId != null) {
      this.favoriteService.removeFavorite(patientId, favoriteId).subscribe({
        next: () => {
          this.favorites.update((favorites) => favorites.filter((favorite) => favorite.id !== favoriteId));
          if (this.showFavoritesOnly()) {
            this.loadContents();
          }
        },
        error: () => this.errorMessage.set('Favorit konnte nicht entfernt werden.')
      });
    } else {
      this.favoriteService.addFavorite(patientId, content.id).subscribe({
        next: (favorite) => this.favorites.update((favorites) => [...favorites, favorite]),
        error: () => this.errorMessage.set('Favorit konnte nicht gespeichert werden.')
      });
    }
  }

  private readonly ratingByContentId = computed(
    () => new Map(this.ratings().map((rating) => [rating.contentId, rating.sterne]))
  );

  protected ownRating(contentId: number): number {
    return this.ratingByContentId().get(contentId) ?? 0;
  }

  protected rate(content: Content, sterne: number): void {
    const patientId = this.authService.currentUser()?.userId;
    if (!patientId) {
      return;
    }

    this.ratingService.rate(patientId, content.id, sterne).subscribe({
      next: (rating) =>
        this.ratings.update((ratings) => [
          ...ratings.filter((existing) => existing.contentId !== rating.contentId),
          rating
        ]),
      error: () => this.errorMessage.set('Bewertung konnte nicht gespeichert werden.')
    });
  }

  private loadRatings(): void {
    const patientId = this.authService.currentUser()?.userId;
    if (!patientId) {
      return;
    }

    this.ratingService.getRatings(patientId).subscribe({
      next: (ratings) => this.ratings.set(ratings),
      error: () => this.errorMessage.set('Bewertungen konnten nicht geladen werden.')
    });
  }

  private loadFavorites(): void {
    const patientId = this.authService.currentUser()?.userId;
    if (!patientId) {
      return;
    }

    this.favoriteService.getFavorites(patientId).subscribe({
      next: (favorites) => this.favorites.set(favorites),
      error: () => this.errorMessage.set('Favoriten konnten nicht geladen werden.')
    });
  }

  private loadModules(): void {
    const patientId = this.authService.currentUser()?.userId;
    if (!patientId) {
      return;
    }

    this.modulService.getUnlockedModules(patientId).subscribe({
      next: (modules) => {
        this.modules.set(modules);
        this.isLoadingModules.set(false);
      },
      error: () => {
        this.errorMessage.set('Module konnten nicht geladen werden.');
        this.isLoadingModules.set(false);
      }
    });
  }

  private loadContents(): void {
    const patientId = this.authService.currentUser()?.userId;
    if (!patientId) {
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.contentService
      .getContents(patientId, {
        categoryId: this.selectedCategoryId(),
        search: this.searchTerm(),
        favoritesOnly: this.showFavoritesOnly(),
        page: this.currentPage(),
        size: PAGE_SIZE
      })
      .subscribe({
        next: (page) => {
          if (page.content.length === 0 && page.page > 0) {
            this.goToPage(page.page - 1);
            return;
          }
          this.contents.set(page.content);
          this.totalPages.set(page.totalPages);
          this.totalElements.set(page.totalElements);
          this.isLoading.set(false);
        },
        error: () => {
          this.errorMessage.set('Tipps konnten nicht geladen werden.');
          this.isLoading.set(false);
        }
      });
  }
}
