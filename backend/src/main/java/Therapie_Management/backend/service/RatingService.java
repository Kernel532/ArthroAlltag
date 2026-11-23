package Therapie_Management.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Therapie_Management.backend.dto.RatingRequest;
import Therapie_Management.backend.dto.RatingResponse;
import Therapie_Management.backend.entity.Content;
import Therapie_Management.backend.entity.ContentRating;
import Therapie_Management.backend.entity.Patient;
import Therapie_Management.backend.repository.ContentRatingRepository;

@Service
@Transactional
public class RatingService {

    private final ContentRatingRepository ratingRepository;
    private final ContentAccessService accessService;
    private final EntityFinder finder;

    public RatingService(ContentRatingRepository ratingRepository, ContentAccessService accessService,
                          EntityFinder finder) {
        this.ratingRepository = ratingRepository;
        this.accessService = accessService;
        this.finder = finder;
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> getRatings(String patientId) {
        finder.patient(patientId);
        return ratingRepository.findByPatientId(patientId).stream()
            .map(rating -> new RatingResponse(rating.getContent().getId(), rating.getSterne()))
            .toList();
    }

    /** Legt die Bewertung an oder ueberschreibt eine bereits abgegebene (je Patient und Tipp nur eine). */
    public RatingResponse rate(String patientId, Integer contentId, RatingRequest request) {
        Patient patient = finder.patient(patientId);
        Content content = finder.content(contentId);
        accessService.ensureVisible(patientId, content);

        ContentRating rating = ratingRepository.findByPatientIdAndContentId(patientId, contentId)
            .orElseGet(() -> new ContentRating(patient, content, request.sterne()));
        rating.setSterne(request.sterne());
        ratingRepository.save(rating);

        return new RatingResponse(contentId, rating.getSterne());
    }
}
