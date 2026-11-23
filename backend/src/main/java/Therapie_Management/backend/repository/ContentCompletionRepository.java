package Therapie_Management.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Therapie_Management.backend.entity.ContentCompletion;

public interface ContentCompletionRepository extends JpaRepository<ContentCompletion, Integer> {

    List<ContentCompletion> findByPatientId(String patientId);

    boolean existsByPatientIdAndContentId(String patientId, Integer contentId);

    void deleteByPatientIdAndContentId(String patientId, Integer contentId);

    void deleteByContentId(Integer contentId);
}
