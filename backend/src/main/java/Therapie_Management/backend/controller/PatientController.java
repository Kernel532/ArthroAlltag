package Therapie_Management.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Therapie_Management.backend.dto.ContentResponse;
import Therapie_Management.backend.dto.FavoriteCreateRequest;
import Therapie_Management.backend.dto.FavoriteResponse;
import Therapie_Management.backend.dto.ModuleResponse;
import Therapie_Management.backend.dto.PageResponse;
import Therapie_Management.backend.dto.PatientResponse;
import Therapie_Management.backend.dto.ProgressResponse;
import Therapie_Management.backend.dto.RatingRequest;
import Therapie_Management.backend.dto.RatingResponse;
import Therapie_Management.backend.service.FavoriteService;
import Therapie_Management.backend.service.ModuleService;
import Therapie_Management.backend.service.PatientService;
import Therapie_Management.backend.service.ProgressService;
import Therapie_Management.backend.service.RatingService;
import jakarta.validation.Valid;

/**
 * Nur lesende Endpoints (bis auf Favoriten setzen/entfernen).
 *
 * @PreAuthorize auf Klassenebene gilt fuer alle Methoden: der Aufrufer muss die Rolle
 * PATIENT haben UND die patientId aus dem Pfad muss exakt der eigenen User-ID entsprechen
 * (authentication.name wird vom JwtAuthenticationFilter aus dem Token gesetzt). Damit kann
 * Patient v110001 z.B. nicht ueber /api/patients/v110002/favorites auf fremde Daten zugreifen.
 */
@RestController
@RequestMapping("/api/patients/{patientId}")
@PreAuthorize("hasRole('PATIENT') and #patientId == authentication.name")
public class PatientController {

    private final PatientService patientService;
    private final FavoriteService favoriteService;
    private final ModuleService moduleService;
    private final RatingService ratingService;
    private final ProgressService progressService;

    public PatientController(PatientService patientService, FavoriteService favoriteService,
                              ModuleService moduleService, RatingService ratingService,
                              ProgressService progressService) {
        this.patientService = patientService;
        this.favoriteService = favoriteService;
        this.moduleService = moduleService;
        this.ratingService = ratingService;
        this.progressService = progressService;
    }

    @GetMapping
    public ResponseEntity<PatientResponse> getPatient(@P("patientId") @PathVariable String patientId) {
        return ResponseEntity.ok(patientService.getPatient(patientId));
    }

    @GetMapping("/contents")
    public ResponseEntity<PageResponse<ContentResponse>> getContents(
            @P("patientId") @PathVariable String patientId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean favoritesOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PageResponse.DEFAULT_SIZE) int size) {
        return ResponseEntity.ok(
            patientService.getContentsByCategory(patientId, categoryId, search, favoritesOnly, page, size));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<FavoriteResponse>> getFavorites(@P("patientId") @PathVariable String patientId) {
        return ResponseEntity.ok(patientService.getFavorites(patientId));
    }

    @PostMapping("/favorites")
    public ResponseEntity<FavoriteResponse> addFavorite(
            @P("patientId") @PathVariable String patientId,
            @Valid @RequestBody FavoriteCreateRequest request) {
        FavoriteResponse favorite = favoriteService.addFavorite(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(favorite);
    }

    @DeleteMapping("/favorites/{favoriteId}")
    public ResponseEntity<Void> removeFavorite(
            @P("patientId") @PathVariable String patientId,
            @PathVariable Integer favoriteId) {
        favoriteService.removeFavorite(patientId, favoriteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ratings")
    public ResponseEntity<List<RatingResponse>> getRatings(@P("patientId") @PathVariable String patientId) {
        return ResponseEntity.ok(ratingService.getRatings(patientId));
    }

    /** Bewertet einen Tipp mit 1-5 Sternen; eine erneute Bewertung ueberschreibt die alte. */
    @PutMapping("/ratings/{contentId}")
    public ResponseEntity<RatingResponse> rate(
            @P("patientId") @PathVariable String patientId,
            @PathVariable Integer contentId,
            @Valid @RequestBody RatingRequest request) {
        return ResponseEntity.ok(ratingService.rate(patientId, contentId, request));
    }

    @GetMapping("/progress")
    public ResponseEntity<ProgressResponse> getProgress(@P("patientId") @PathVariable String patientId) {
        return ResponseEntity.ok(progressService.getProgress(patientId));
    }

    /** Markiert einen Tipp als ausprobiert bzw. eine Uebung als durchgefuehrt. */
    @PutMapping("/completions/{contentId}")
    public ResponseEntity<Void> markCompleted(
            @P("patientId") @PathVariable String patientId,
            @PathVariable Integer contentId) {
        progressService.markCompleted(patientId, contentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/completions/{contentId}")
    public ResponseEntity<Void> unmarkCompleted(
            @P("patientId") @PathVariable String patientId,
            @PathVariable Integer contentId) {
        progressService.unmarkCompleted(patientId, contentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/modules")
    public ResponseEntity<List<ModuleResponse>> getUnlockedModules(@P("patientId") @PathVariable String patientId) {
        return ResponseEntity.ok(moduleService.getUnlockedModules(patientId));
    }
}
