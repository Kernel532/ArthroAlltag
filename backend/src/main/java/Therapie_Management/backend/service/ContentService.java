package Therapie_Management.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Therapie_Management.backend.dto.CategoryResponse;
import Therapie_Management.backend.dto.ContentCreateRequest;
import Therapie_Management.backend.dto.ContentResponse;
import Therapie_Management.backend.dto.ModuleResponse;
import Therapie_Management.backend.dto.PageResponse;
import Therapie_Management.backend.entity.Category;
import Therapie_Management.backend.entity.Content;
import Therapie_Management.backend.entity.Module;
import Therapie_Management.backend.entity.Therapeut;
import Therapie_Management.backend.repository.ContentCompletionRepository;
import Therapie_Management.backend.repository.ContentRatingRepository;
import Therapie_Management.backend.repository.ContentRepository;

@Service
@Transactional
public class ContentService {

    private final ContentRepository contentRepository;
    private final ContentRatingRepository ratingRepository;
    private final ContentCompletionRepository completionRepository;
    private final ContentAccessService accessService;
    private final EntityFinder finder;

    public ContentService(ContentRepository contentRepository, ContentRatingRepository ratingRepository,
                           ContentCompletionRepository completionRepository,
                           ContentAccessService accessService, EntityFinder finder) {
        this.contentRepository = contentRepository;
        this.ratingRepository = ratingRepository;
        this.completionRepository = completionRepository;
        this.accessService = accessService;
        this.finder = finder;
    }

    /** Alle Tipps (Therapeuten-Verwaltung), seitenweise. Patienten nutzen {@link #getContentsForPatient}. */
    public PageResponse<ContentResponse> getContents(Integer kategorieId, int page, int size) {
        Pageable pageable = PageResponse.request(page, size, Sort.by("titel", "id"));
        Page<Content> contents = kategorieId != null
            ? contentRepository.findByKategorieId(kategorieId, pageable)
            : contentRepository.findAll(pageable);

        return PageResponse.of(contents, this::toResponse);
    }

    /** Nur Tipps aus Modulen, die dem Patienten freigeschaltet sind. */
    public PageResponse<ContentResponse> getContentsForPatient(String patientId, Integer kategorieId, String search,
                                                                boolean favoritesOnly, int page, int size) {
        Pageable pageable = PageResponse.request(page, size, Sort.by("titel", "id"));
        return PageResponse.of(
            accessService.getVisibleContents(patientId, kategorieId, search, favoritesOnly, pageable),
            this::toResponse);
    }

    public ContentResponse getContent(Integer contentId) {
        return toResponse(finder.content(contentId));
    }

    public ContentResponse createContent(String therapeutId, ContentCreateRequest request) {
        Therapeut therapeut = finder.therapeut(therapeutId);
        Category kategorie = finder.category(request.kategorieId());
        Module modul = finder.modul(request.modulId());

        Content content = new Content();
        applyRequest(content, request, kategorie, modul);
        content.setErstelltDurch(therapeut);

        return toResponse(contentRepository.save(content));
    }

    public ContentResponse updateContent(String therapeutId, Integer contentId, ContentCreateRequest request) {
        finder.therapeut(therapeutId);
        Content content = finder.content(contentId);
        Category kategorie = finder.category(request.kategorieId());
        Module modul = finder.modul(request.modulId());

        applyRequest(content, request, kategorie, modul);

        return toResponse(contentRepository.save(content));
    }

    public void deleteContent(String therapeutId, Integer contentId) {
        finder.therapeut(therapeutId);
        Content content = finder.content(contentId);
        ratingRepository.deleteByContentId(contentId);
        completionRepository.deleteByContentId(contentId);
        contentRepository.delete(content);
    }

    private void applyRequest(Content content, ContentCreateRequest request, Category kategorie, Module modul) {
        content.setTitel(request.titel());
        content.setBeschreibung(request.beschreibung());
        content.setKategorie(kategorie);
        content.setModul(modul);
        content.setBildUrl(request.bildUrl());
        content.setSchwierigkeitsgrad(request.schwierigkeitsgrad());
        content.setHilfsmittel(request.hilfsmittel());
    }

    /**
     * kategorie/erstelltDurch sind LAZY (siehe Content-Entity) - jeder Aufruf hier loest bei
     * einer Liste von Tipps klassisches N+1 aus. Bei der Datenmenge dieses Projekts (siehe
     * data.sql) faellt das nicht ins Gewicht; bei echten Datenmengen wuerde man stattdessen
     * z.B. @EntityGraph oder eine JOIN-FETCH-Query verwenden.
     */
    private ContentResponse toResponse(Content content) {
        CategoryResponse kategorie = new CategoryResponse(content.getKategorie().getId(), content.getKategorie().getName());
        String erstelltDurchName = content.getErstelltDurch().getVorname() + " " + content.getErstelltDurch().getNachname();
        Double durchschnitt = ratingRepository.findAverageSterneByContentId(content.getId());
        Module modul = content.getModul();
        ModuleResponse modulResponse = modul == null
            ? null
            : new ModuleResponse(modul.getId(), modul.getName(), modul.getReihenfolge(), modul.getBeschreibung());

        return new ContentResponse(
            content.getId(),
            content.getTitel(),
            content.getBeschreibung(),
            kategorie,
            modulResponse,
            content.getBildUrl(),
            content.getSchwierigkeitsgrad(),
            content.getHilfsmittel(),
            erstelltDurchName,
            content.getErstelltAm(),
            content.getAktualisiertAm(),
            durchschnitt == null ? null : Math.round(durchschnitt * 10) / 10.0,
            ratingRepository.countByContentId(content.getId())
        );
    }
}
