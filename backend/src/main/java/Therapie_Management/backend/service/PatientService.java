package Therapie_Management.backend.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Therapie_Management.backend.dto.ContentResponse;
import Therapie_Management.backend.dto.FavoriteResponse;
import Therapie_Management.backend.dto.PageResponse;
import Therapie_Management.backend.dto.PatientResponse;
import Therapie_Management.backend.entity.Patient;
import Therapie_Management.backend.entity.Therapeut;
import Therapie_Management.backend.exception.ValidationException;
import Therapie_Management.backend.repository.PatientFavoriteRepository;
import Therapie_Management.backend.repository.PatientRepository;

@Service
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientFavoriteRepository patientFavoriteRepository;
    private final ContentService contentService;
    private final EntityFinder finder;
    private final AssignmentGuard assignmentGuard;

    public PatientService(PatientRepository patientRepository, PatientFavoriteRepository patientFavoriteRepository,
                           ContentService contentService, EntityFinder finder, AssignmentGuard assignmentGuard) {
        this.patientRepository = patientRepository;
        this.patientFavoriteRepository = patientFavoriteRepository;
        this.contentService = contentService;
        this.finder = finder;
        this.assignmentGuard = assignmentGuard;
    }

    public PatientResponse getPatient(String patientId) {
        return toResponse(finder.patient(patientId));
    }

    static final Sort PATIENT_ORDER = Sort.by("nachname", "vorname", "id");

    /** Patienten, die dem Therapeuten zugeordnet sind (seitenweise). */
    public PageResponse<PatientResponse> getPatientsOfTherapeut(String therapeutId, int page, int size) {
        return PageResponse.of(
            patientRepository.findByTherapeutId(therapeutId, PageResponse.request(page, size, PATIENT_ORDER)),
            this::toResponse);
    }

    /** Patienten ohne Therapeut - sie koennen von einem Therapeuten uebernommen werden (seitenweise). */
    public PageResponse<PatientResponse> getUnassignedPatients(int page, int size) {
        return PageResponse.of(
            patientRepository.findByTherapeutIsNull(PageResponse.request(page, size, PATIENT_ORDER)),
            this::toResponse);
    }

    @Transactional
    public PatientResponse assignPatient(String therapeutId, String patientId) {
        Therapeut therapeut = finder.therapeut(therapeutId);
        Patient patient = finder.patient(patientId);

        if (patient.getTherapeut() != null) {
            throw new ValidationException("Patient ist bereits einem Therapeuten zugeordnet");
        }

        patient.setTherapeut(therapeut);
        return toResponse(patient);
    }

    @Transactional
    public void unassignPatient(String therapeutId, String patientId) {
        Patient patient = finder.patient(patientId);

        assignmentGuard.ensureAssigned(therapeutId, patient);

        patient.setTherapeut(null);
    }

    public PageResponse<ContentResponse> getContentsByCategory(String patientId, Integer kategorieId, String search,
                                                                boolean favoritesOnly, int page, int size) {
        finder.patient(patientId);
        return contentService.getContentsForPatient(patientId, kategorieId, search, favoritesOnly, page, size);
    }

    public List<FavoriteResponse> getFavorites(String patientId) {
        finder.patient(patientId);
        return patientFavoriteRepository.findByPatientId(patientId).stream()
            .map(favorite -> new FavoriteResponse(favorite.getId(), favorite.getTipp().getId(), favorite.getHinzugefuegtAm()))
            .toList();
    }

    private PatientResponse toResponse(Patient patient) {
        return new PatientResponse(patient.getId(), patient.getVorname(), patient.getNachname(),
            patient.getDiagnostik(), patient.getTherapeut() == null ? null : patient.getTherapeut().getId());
    }
}
