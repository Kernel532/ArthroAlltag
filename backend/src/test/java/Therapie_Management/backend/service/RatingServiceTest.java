package Therapie_Management.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Therapie_Management.backend.dto.RatingRequest;
import Therapie_Management.backend.dto.RatingResponse;
import Therapie_Management.backend.entity.Content;
import Therapie_Management.backend.entity.ContentRating;
import Therapie_Management.backend.entity.Patient;
import Therapie_Management.backend.exception.ResourceNotFoundException;
import Therapie_Management.backend.repository.ContentRatingRepository;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private ContentRatingRepository ratingRepository;

    @Mock
    private ContentAccessService accessService;

    @Mock
    private EntityFinder finder;

    @InjectMocks
    private RatingService ratingService;

    private final Patient patient = new Patient("v110001", "Elena", "Fischer", null, "hash", "Rhizarthrose", null);

    private Content tipp() {
        Content tipp = new Content();
        tipp.setId(1);
        return tipp;
    }

    @Test
    void rate_legtNeueBewertungAn() {
        when(finder.patient("v110001")).thenReturn(patient);
        when(finder.content(1)).thenReturn(tipp());
        when(ratingRepository.findByPatientIdAndContentId("v110001", 1)).thenReturn(Optional.empty());

        RatingResponse response = ratingService.rate("v110001", 1, new RatingRequest(4));

        assertThat(response.sterne()).isEqualTo(4);
        verify(ratingRepository).save(any(ContentRating.class));
    }

    @Test
    void rate_ueberschreibtVorhandeneBewertung() {
        ContentRating vorhanden = new ContentRating(patient, tipp(), 2);
        when(finder.patient("v110001")).thenReturn(patient);
        when(finder.content(1)).thenReturn(tipp());
        when(ratingRepository.findByPatientIdAndContentId("v110001", 1)).thenReturn(Optional.of(vorhanden));

        ratingService.rate("v110001", 1, new RatingRequest(5));

        assertThat(vorhanden.getSterne()).isEqualTo(5);
    }

    @Test
    void rate_wirftResourceNotFound_beiUnbekanntemTipp() {
        when(finder.patient("v110001")).thenReturn(patient);
        when(finder.content(99)).thenThrow(new ResourceNotFoundException("Tipp nicht gefunden: 99"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> ratingService.rate("v110001", 99, new RatingRequest(3)))
            .isInstanceOf(ResourceNotFoundException.class);
        verify(ratingRepository, never()).save(any());
    }
}
