package Therapie_Management.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import Therapie_Management.backend.dto.PageResponse;
import Therapie_Management.backend.dto.PatientResponse;
import Therapie_Management.backend.dto.UnlockModuleRequest;
import Therapie_Management.backend.entity.Patient;
import Therapie_Management.backend.entity.Therapeut;
import Therapie_Management.backend.exception.ForbiddenException;
import Therapie_Management.backend.exception.ValidationException;
import Therapie_Management.backend.repository.PatientRepository;

/** Prueft die Therapeuten-Zuordnung: Uebernehmen, Abgeben und die Zugriffsbeschraenkung. */
@ExtendWith(MockitoExtension.class)
class TherapeutZuordnungTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private EntityFinder finder;

    @Spy
    private AssignmentGuard assignmentGuard = new AssignmentGuard();

    @InjectMocks
    private PatientService patientService;

    @InjectMocks
    private ModuleService moduleService;

    private Therapeut therapeut(String id) {
        return new Therapeut(id, "Maren", "Klein", null, "hash", "m@example.org", "Ergo");
    }

    private Patient patient(Therapeut zustaendig) {
        Patient patient = new Patient("v110001", "Elena", "Fischer", null, "hash", "Rhizarthrose", null);
        patient.setTherapeut(zustaendig);
        return patient;
    }

    @Test
    void assignPatient_ordnetFreienPatientenDemTherapeutenZu() {
        Therapeut maren = therapeut("v220001");
        Patient frei = patient(null);
        when(finder.therapeut("v220001")).thenReturn(maren);
        when(finder.patient("v110001")).thenReturn(frei);

        PatientResponse response = patientService.assignPatient("v220001", "v110001");

        assertThat(response.therapeutId()).isEqualTo("v220001");
    }

    @Test
    void assignPatient_wirftValidationException_wennBereitsZugeordnet() {
        when(finder.therapeut("v220001")).thenReturn(therapeut("v220001"));
        when(finder.patient("v110001")).thenReturn(patient(therapeut("v220002")));

        assertThatThrownBy(() -> patientService.assignPatient("v220001", "v110001"))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void unassignPatient_wirftForbiddenException_beiFremdemPatienten() {
        when(finder.patient("v110001")).thenReturn(patient(therapeut("v220002")));

        assertThatThrownBy(() -> patientService.unassignPatient("v220001", "v110001"))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void unlockModule_wirftForbiddenException_beiNichtZugeordnetemPatienten() {
        when(finder.therapeut("v220001")).thenReturn(therapeut("v220001"));
        when(finder.patient("v110001")).thenReturn(patient(therapeut("v220002")));

        assertThatThrownBy(() -> moduleService.unlockModule("v220001", new UnlockModuleRequest("v110001", 1)))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getUnassignedPatients_liefertNurPatientenOhneTherapeut() {
        when(patientRepository.findByTherapeutIsNull(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(patient(null))));

        PageResponse<PatientResponse> page = patientService.getUnassignedPatients(0, 10);

        assertThat(page.content()).hasSize(1);
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void getPatientsOfTherapeut_begrenztSeitengroesseUndSortiertNachName() {
        when(patientRepository.findByTherapeutId(eq("v220001"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        patientService.getPatientsOfTherapeut("v220001", -3, 5000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(patientRepository).findByTherapeutId(eq("v220001"), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(PageResponse.MAX_SIZE);
        assertThat(captor.getValue().getSort().getOrderFor("nachname")).isNotNull();
    }
}
