package Therapie_Management.backend.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Therapie_Management.backend.dto.PageResponse;
import Therapie_Management.backend.dto.ProgressResponse;
import Therapie_Management.backend.dto.ProgressResponse.CategoryProgress;
import Therapie_Management.backend.dto.ProgressResponse.CompletedContent;
import Therapie_Management.backend.entity.Content;
import Therapie_Management.backend.entity.ContentCompletion;
import Therapie_Management.backend.entity.Patient;
import Therapie_Management.backend.repository.ContentCompletionRepository;
import Therapie_Management.backend.repository.PatientRepository;

@Service
@Transactional
public class ProgressService {

    private final ContentCompletionRepository completionRepository;
    private final PatientRepository patientRepository;
    private final ContentAccessService accessService;
    private final EntityFinder finder;
    private final AssignmentGuard assignmentGuard;

    public ProgressService(ContentCompletionRepository completionRepository,
                            PatientRepository patientRepository, ContentAccessService accessService,
                            EntityFinder finder, AssignmentGuard assignmentGuard) {
        this.completionRepository = completionRepository;
        this.patientRepository = patientRepository;
        this.accessService = accessService;
        this.finder = finder;
        this.assignmentGuard = assignmentGuard;
    }

    /** Markiert einen Tipp als ausprobiert/eine Uebung als durchgefuehrt; wiederholtes Markieren ist unschaedlich. */
    public void markCompleted(String patientId, Integer contentId) {
        Patient patient = finder.patient(patientId);
        Content content = finder.content(contentId);

        accessService.ensureVisible(patientId, content);

        if (!completionRepository.existsByPatientIdAndContentId(patientId, contentId)) {
            completionRepository.save(new ContentCompletion(patient, content));
        }
    }

    public void unmarkCompleted(String patientId, Integer contentId) {
        finder.patient(patientId);
        completionRepository.deleteByPatientIdAndContentId(patientId, contentId);
    }

    @Transactional(readOnly = true)
    public ProgressResponse getProgress(String patientId) {
        return buildProgress(finder.patient(patientId));
    }

    /** Fortschritt der dem Therapeuten zugeordneten Patienten (Uebersicht), gleiche Sortierung/Seite wie die Patientenliste. */
    @Transactional(readOnly = true)
    public PageResponse<ProgressResponse> getProgressOfTherapeutPatients(String therapeutId, int page, int size) {
        return PageResponse.of(
            patientRepository.findByTherapeutId(therapeutId, PageResponse.request(page, size, PatientService.PATIENT_ORDER)),
            this::buildProgress);
    }

    /** Fortschritt eines einzelnen Patienten - nur fuer den zustaendigen Therapeuten. */
    @Transactional(readOnly = true)
    public ProgressResponse getProgressForTherapeut(String therapeutId, String patientId) {
        Patient patient = finder.patient(patientId);
        assignmentGuard.ensureAssigned(therapeutId, patient);
        return buildProgress(patient);
    }

    /** Zaehlt nur Inhalte aus Modulen, die dem Patienten freigeschaltet sind. */
    private ProgressResponse buildProgress(Patient patient) {
        List<Content> contents = accessService.getVisibleContents(patient.getId(), null);
        List<ContentCompletion> completions = visibleCompletions(patient, contents);

        return new ProgressResponse(patient.getId(), patient.getVorname(), patient.getNachname(),
            contents.size(), completions.size(), countPerCategory(contents, completions), toCompletedContents(completions));
    }

    private List<ContentCompletion> visibleCompletions(Patient patient, List<Content> visibleContents) {
        Set<Integer> visibleIds = visibleContents.stream().map(Content::getId).collect(Collectors.toSet());
        return completionRepository.findByPatientId(patient.getId()).stream()
            .filter(completion -> visibleIds.contains(completion.getContent().getId()))
            .toList();
    }

    private List<CategoryProgress> countPerCategory(List<Content> contents, List<ContentCompletion> completions) {
        Set<Integer> completedIds = completions.stream()
            .map(completion -> completion.getContent().getId())
            .collect(Collectors.toSet());

        Map<String, int[]> perCategory = new LinkedHashMap<>();
        for (Content content : contents) {
            int[] counts = perCategory.computeIfAbsent(content.getKategorie().getName(), name -> new int[2]);
            counts[0]++;
            if (completedIds.contains(content.getId())) {
                counts[1]++;
            }
        }

        return perCategory.entrySet().stream()
            .map(entry -> new CategoryProgress(entry.getKey(), entry.getValue()[0], entry.getValue()[1]))
            .toList();
    }

    private List<CompletedContent> toCompletedContents(List<ContentCompletion> completions) {
        return completions.stream()
            .sorted(Comparator.comparing(ContentCompletion::getAbgeschlossenAm).reversed())
            .map(completion -> new CompletedContent(
                completion.getContent().getId(),
                completion.getContent().getTitel(),
                completion.getContent().getKategorie().getName(),
                completion.getAbgeschlossenAm()))
            .toList();
    }
}
