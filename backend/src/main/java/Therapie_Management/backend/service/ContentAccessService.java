package Therapie_Management.backend.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Therapie_Management.backend.entity.Content;
import Therapie_Management.backend.entity.PatientModuleAccess;
import Therapie_Management.backend.exception.ForbiddenException;
import Therapie_Management.backend.repository.ContentRepository;
import Therapie_Management.backend.repository.PatientModuleAccessRepository;

/**
 * Zentrale Regel: Ein Patient sieht nur Inhalte aus Modulen, die ihm freigeschaltet wurden.
 * Inhalte ohne Modul sind fuer Patienten nie sichtbar.
 */
@Service
@Transactional(readOnly = true)
public class ContentAccessService {

    private final ContentRepository contentRepository;
    private final PatientModuleAccessRepository moduleAccessRepository;

    public ContentAccessService(ContentRepository contentRepository,
                                 PatientModuleAccessRepository moduleAccessRepository) {
        this.contentRepository = contentRepository;
        this.moduleAccessRepository = moduleAccessRepository;
    }

    /** Sichtbare Inhalte des Patienten, optional auf eine Kategorie eingeschraenkt. */
    public List<Content> getVisibleContents(String patientId, Integer kategorieId) {
        Set<Integer> modulIds = unlockedModuleIds(patientId);
        if (modulIds.isEmpty()) {
            return List.of();
        }
        return kategorieId != null
            ? contentRepository.findByModulIdInAndKategorieId(modulIds, kategorieId)
            : contentRepository.findByModulIdIn(modulIds);
    }

    /** Wie {@link #getVisibleContents}, aber seitenweise mit Textsuche und optionalem Favoriten-Filter. */
    public Page<Content> getVisibleContents(String patientId, Integer kategorieId, String search,
                                             boolean favoritesOnly, Pageable pageable) {
        Set<Integer> modulIds = unlockedModuleIds(patientId);
        if (modulIds.isEmpty()) {
            return Page.empty(pageable);
        }
        String pattern = "%" + (search == null ? "" : search.trim().toLowerCase()) + "%";
        return contentRepository.searchVisible(modulIds, kategorieId, pattern, favoritesOnly, patientId, pageable);
    }

    public void ensureVisible(String patientId, Content content) {
        boolean visible = content.getModul() != null && unlockedModuleIds(patientId).contains(content.getModul().getId());
        if (!visible) {
            throw new ForbiddenException("Dieser Inhalt ist fuer dich nicht freigeschaltet");
        }
    }

    private Set<Integer> unlockedModuleIds(String patientId) {
        return moduleAccessRepository.findByPatientId(patientId).stream()
            .map(PatientModuleAccess::getModul)
            .map(modul -> modul.getId())
            .collect(Collectors.toSet());
    }
}
