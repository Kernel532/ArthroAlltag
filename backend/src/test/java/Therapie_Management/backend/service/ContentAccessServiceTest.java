package Therapie_Management.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Therapie_Management.backend.entity.Content;
import Therapie_Management.backend.entity.Module;
import Therapie_Management.backend.entity.Patient;
import Therapie_Management.backend.entity.PatientModuleAccess;
import Therapie_Management.backend.exception.ForbiddenException;
import Therapie_Management.backend.repository.ContentRepository;
import Therapie_Management.backend.repository.PatientModuleAccessRepository;

@ExtendWith(MockitoExtension.class)
class ContentAccessServiceTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private PatientModuleAccessRepository moduleAccessRepository;

    @InjectMocks
    private ContentAccessService accessService;

    private final Patient patient = new Patient("v110001", "Elena", "Fischer", null, "hash", "Rhizarthrose", null);

    private Content contentInModul(Integer modulId) {
        Content content = new Content();
        content.setId(1);
        if (modulId != null) {
            content.setModul(new Module(modulId, "Modul " + modulId, modulId, null));
        }
        return content;
    }

    private void freigeschaltet(int modulId) {
        Module modul = new Module(modulId, "Modul " + modulId, modulId, null);
        when(moduleAccessRepository.findByPatientId("v110001"))
            .thenReturn(List.of(new PatientModuleAccess(patient, modul, null)));
    }

    @Test
    void getVisibleContents_ohneFreigeschalteteModule_istLeerOhneContentAbfrage() {
        when(moduleAccessRepository.findByPatientId("v110001")).thenReturn(List.of());

        assertThat(accessService.getVisibleContents("v110001", null)).isEmpty();
        verifyNoInteractions(contentRepository);
    }

    @Test
    void getVisibleContents_filtertNachFreigeschaltetenModulen() {
        freigeschaltet(2);
        Content content = contentInModul(2);
        when(contentRepository.findByModulIdIn(Set.of(2))).thenReturn(List.of(content));

        assertThat(accessService.getVisibleContents("v110001", null)).containsExactly(content);
    }

    @Test
    void ensureVisible_wirftForbiddenException_beiNichtFreigeschaltetemModul() {
        freigeschaltet(1);

        assertThatThrownBy(() -> accessService.ensureVisible("v110001", contentInModul(3)))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void ensureVisible_wirftForbiddenException_beiContentOhneModul() {
        assertThatThrownBy(() -> accessService.ensureVisible("v110001", contentInModul(null)))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void ensureVisible_erlaubtFreigeschaltetesModul() {
        freigeschaltet(1);

        assertThatCode(() -> accessService.ensureVisible("v110001", contentInModul(1))).doesNotThrowAnyException();
    }
}
