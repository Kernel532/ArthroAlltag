package Therapie_Management.backend.service;

import org.springframework.stereotype.Component;

import Therapie_Management.backend.entity.Category;
import Therapie_Management.backend.entity.Content;
import Therapie_Management.backend.entity.Module;
import Therapie_Management.backend.entity.Patient;
import Therapie_Management.backend.entity.Therapeut;
import Therapie_Management.backend.exception.ResourceNotFoundException;
import Therapie_Management.backend.repository.CategoryRepository;
import Therapie_Management.backend.repository.ContentRepository;
import Therapie_Management.backend.repository.ModuleRepository;
import Therapie_Management.backend.repository.PatientRepository;
import Therapie_Management.backend.repository.TherapeutRepository;

/** Zentrale Stelle fuer "laden oder 404" - ersetzt die in jedem Service kopierten find...OrThrow-Methoden. */
@Component
public class EntityFinder {

    private final PatientRepository patientRepository;
    private final TherapeutRepository therapeutRepository;
    private final ContentRepository contentRepository;
    private final ModuleRepository moduleRepository;
    private final CategoryRepository categoryRepository;

    public EntityFinder(PatientRepository patientRepository, TherapeutRepository therapeutRepository,
                         ContentRepository contentRepository, ModuleRepository moduleRepository,
                         CategoryRepository categoryRepository) {
        this.patientRepository = patientRepository;
        this.therapeutRepository = therapeutRepository;
        this.contentRepository = contentRepository;
        this.moduleRepository = moduleRepository;
        this.categoryRepository = categoryRepository;
    }

    public Patient patient(String patientId) {
        return patientRepository.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient nicht gefunden: " + patientId));
    }

    public Therapeut therapeut(String therapeutId) {
        return therapeutRepository.findById(therapeutId)
            .orElseThrow(() -> new ResourceNotFoundException("Therapeut nicht gefunden: " + therapeutId));
    }

    public Content content(Integer contentId) {
        return contentRepository.findById(contentId)
            .orElseThrow(() -> new ResourceNotFoundException("Tipp nicht gefunden: " + contentId));
    }

    public Module modul(Integer modulId) {
        return moduleRepository.findById(modulId)
            .orElseThrow(() -> new ResourceNotFoundException("Modul nicht gefunden: " + modulId));
    }

    public Category category(Integer kategorieId) {
        return categoryRepository.findById(kategorieId)
            .orElseThrow(() -> new ResourceNotFoundException("Kategorie nicht gefunden: " + kategorieId));
    }
}
