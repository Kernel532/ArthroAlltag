package Therapie_Management.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import Therapie_Management.backend.dto.ProgressResponse;
import Therapie_Management.backend.entity.Category;
import Therapie_Management.backend.entity.Content;
import Therapie_Management.backend.entity.ContentCompletion;
import Therapie_Management.backend.entity.Patient;
import Therapie_Management.backend.entity.Therapeut;
import Therapie_Management.backend.exception.ForbiddenException;
import Therapie_Management.backend.repository.ContentCompletionRepository;
import Therapie_Management.backend.repository.PatientRepository;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private ContentCompletionRepository completionRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ContentAccessService accessService;

    @Mock
    private EntityFinder finder;

    @Spy
    private AssignmentGuard assignmentGuard = new AssignmentGuard();

    @InjectMocks
    private ProgressService progressService;

    private final Patient patient = new Patient("v110001", "Elena", "Fischer", null, "hash", "Rhizarthrose", null);

    private Content content(int id, String kategorie) {
        Category category = new Category();
        category.setName(kategorie);
        Content content = new Content();
        content.setId(id);
        content.setTitel("Tipp " + id);
        content.setKategorie(category);
        return content;
    }

    @Test
    void getProgress_zaehltAbgeschlossenesJeKategorie() {
        Content kueche = content(1, "Kueche");
        Content uebung1 = content(2, "Uebungen");
        Content uebung2 = content(3, "Uebungen");
        ContentCompletion erledigt = new ContentCompletion(patient, uebung1);
        erledigt.setAbgeschlossenAm(LocalDateTime.now());

        when(finder.patient("v110001")).thenReturn(patient);
        when(accessService.getVisibleContents("v110001", null)).thenReturn(List.of(kueche, uebung1, uebung2));
        when(completionRepository.findByPatientId("v110001")).thenReturn(List.of(erledigt));

        ProgressResponse progress = progressService.getProgress("v110001");

        assertThat(progress.gesamt()).isEqualTo(3);
        assertThat(progress.abgeschlossen()).isEqualTo(1);
        assertThat(progress.kategorien()).extracting("kategorie", "gesamt", "abgeschlossen")
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple("Kueche", 1, 0),
                org.assertj.core.groups.Tuple.tuple("Uebungen", 2, 1));
    }

    @Test
    void markCompleted_speichertNurEinmal() {
        when(finder.patient("v110001")).thenReturn(patient);
        when(finder.content(1)).thenReturn(content(1, "Kueche"));
        when(completionRepository.existsByPatientIdAndContentId("v110001", 1)).thenReturn(true);

        progressService.markCompleted("v110001", 1);

        verify(completionRepository, never()).save(any());
    }

    @Test
    void getProgressForTherapeut_wirftForbiddenException_beiFremdemPatienten() {
        Therapeut anderer = new Therapeut("v220002", "Stefan", "Mueller", null, "hash", "s@example.org", "Physio");
        patient.setTherapeut(anderer);
        when(finder.patient("v110001")).thenReturn(patient);

        assertThatThrownBy(() -> progressService.getProgressForTherapeut("v220001", "v110001"))
            .isInstanceOf(ForbiddenException.class);
    }
}
