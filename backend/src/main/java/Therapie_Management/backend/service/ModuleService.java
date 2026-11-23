package Therapie_Management.backend.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Therapie_Management.backend.dto.ModuleResponse;
import Therapie_Management.backend.dto.UnlockModuleRequest;
import Therapie_Management.backend.entity.Module;
import Therapie_Management.backend.entity.Patient;
import Therapie_Management.backend.entity.PatientModuleAccess;
import Therapie_Management.backend.entity.Therapeut;
import Therapie_Management.backend.exception.ResourceNotFoundException;
import Therapie_Management.backend.exception.ValidationException;
import Therapie_Management.backend.repository.ModuleRepository;
import Therapie_Management.backend.repository.PatientModuleAccessRepository;
import Therapie_Management.backend.repository.PatientRepository;

@Service
@Transactional
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final PatientRepository patientRepository;
    private final PatientModuleAccessRepository patientModuleAccessRepository;
    private final EntityFinder finder;
    private final AssignmentGuard assignmentGuard;

    public ModuleService(ModuleRepository moduleRepository, PatientRepository patientRepository,
                          PatientModuleAccessRepository patientModuleAccessRepository,
                          EntityFinder finder, AssignmentGuard assignmentGuard) {
        this.moduleRepository = moduleRepository;
        this.patientRepository = patientRepository;
        this.patientModuleAccessRepository = patientModuleAccessRepository;
        this.finder = finder;
        this.assignmentGuard = assignmentGuard;
    }

    /** Fuer GET /api/modules - alle 4 fest vorgegebenen Module, unabhaengig vom Freischaltstatus. */
    public List<ModuleResponse> getAllModules() {
        return moduleRepository.findAllByOrderByReihenfolgeAsc().stream()
            .map(this::toResponse)
            .toList();
    }

    public List<ModuleResponse> getUnlockedModules(String patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient nicht gefunden: " + patientId);
        }

        return patientModuleAccessRepository.findByPatientId(patientId).stream()
            .map(PatientModuleAccess::getModul)
            .sorted(Comparator.comparing(Module::getReihenfolge))
            .map(this::toResponse)
            .toList();
    }

    /** Freischaltstatus eines Patienten aus Therapeuten-Sicht - nur fuer zugeordnete Patienten. */
    public List<ModuleResponse> getUnlockedModulesForTherapeut(String therapeutId, String patientId) {
        assignmentGuard.ensureAssigned(therapeutId, finder.patient(patientId));
        return getUnlockedModules(patientId);
    }

    public void unlockModule(String therapeutId, UnlockModuleRequest request) {
        Therapeut therapeut = finder.therapeut(therapeutId);
        Patient patient = finder.patient(request.patientId());
        assignmentGuard.ensureAssigned(therapeutId, patient);
        Module modul = finder.modul(request.modulId());

        if (patientModuleAccessRepository.existsByPatientIdAndModulId(request.patientId(), request.modulId())) {
            throw new ValidationException("Modul ist fuer diesen Patienten bereits freigeschaltet");
        }

        patientModuleAccessRepository.save(new PatientModuleAccess(patient, modul, therapeut));
    }

    /**
     * Nimmt eine Freischaltung wieder zurueck (z.B. weil sie versehentlich erteilt wurde).
     * Geloescht wird nur der Freischalt-Eintrag - das Modul selbst bleibt bestehen und kann
     * jederzeit erneut freigeschaltet werden.
     */
    public void revokeModule(String therapeutId, String patientId, Integer modulId) {
        finder.therapeut(therapeutId);
        assignmentGuard.ensureAssigned(therapeutId, finder.patient(patientId));
        finder.modul(modulId);

        if (!patientModuleAccessRepository.existsByPatientIdAndModulId(patientId, modulId)) {
            throw new ValidationException("Modul ist fuer diesen Patienten nicht freigeschaltet");
        }

        patientModuleAccessRepository.deleteByPatientIdAndModulId(patientId, modulId);
    }

    private ModuleResponse toResponse(Module modul) {
        return new ModuleResponse(modul.getId(), modul.getName(), modul.getReihenfolge(), modul.getBeschreibung());
    }
}
