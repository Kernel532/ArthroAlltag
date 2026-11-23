package Therapie_Management.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Therapie_Management.backend.dto.FavoriteCreateRequest;
import Therapie_Management.backend.dto.FavoriteResponse;
import Therapie_Management.backend.entity.Content;
import Therapie_Management.backend.entity.Patient;
import Therapie_Management.backend.entity.PatientFavorite;
import Therapie_Management.backend.exception.ForbiddenException;
import Therapie_Management.backend.exception.ResourceNotFoundException;
import Therapie_Management.backend.exception.ValidationException;
import Therapie_Management.backend.repository.PatientFavoriteRepository;

@Service
@Transactional
public class FavoriteService {

    private final PatientFavoriteRepository patientFavoriteRepository;
    private final ContentAccessService accessService;
    private final EntityFinder finder;

    public FavoriteService(PatientFavoriteRepository patientFavoriteRepository, ContentAccessService accessService,
                            EntityFinder finder) {
        this.patientFavoriteRepository = patientFavoriteRepository;
        this.accessService = accessService;
        this.finder = finder;
    }

    public FavoriteResponse addFavorite(String patientId, FavoriteCreateRequest request) {
        Patient patient = finder.patient(patientId);
        Content tipp = finder.content(request.tippId());
        accessService.ensureVisible(patientId, tipp);

        if (patientFavoriteRepository.existsByPatientIdAndTippId(patientId, request.tippId())) {
            throw new ValidationException("Tipp ist bereits favorisiert");
        }

        PatientFavorite saved = patientFavoriteRepository.save(new PatientFavorite(patient, tipp));
        return new FavoriteResponse(saved.getId(), saved.getTipp().getId(), saved.getHinzugefuegtAm());
    }

    public void removeFavorite(String patientId, Integer favoriteId) {
        PatientFavorite favorite = patientFavoriteRepository.findById(favoriteId)
            .orElseThrow(() -> new ResourceNotFoundException("Favorit nicht gefunden: " + favoriteId));

        if (!favorite.getPatient().getId().equals(patientId)) {
            throw new ForbiddenException("Favorit gehoert nicht zu diesem Patienten");
        }

        patientFavoriteRepository.delete(favorite);
    }
}
