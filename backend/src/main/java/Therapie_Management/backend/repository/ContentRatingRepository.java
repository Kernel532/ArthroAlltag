package Therapie_Management.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import Therapie_Management.backend.entity.ContentRating;

public interface ContentRatingRepository extends JpaRepository<ContentRating, Integer> {

    List<ContentRating> findByPatientId(String patientId);

    Optional<ContentRating> findByPatientIdAndContentId(String patientId, Integer contentId);

    long countByContentId(Integer contentId);

    /** Liefert null, solange es noch keine Bewertung gibt. */
    @Query("select avg(r.sterne) from ContentRating r where r.content.id = :contentId")
    Double findAverageSterneByContentId(Integer contentId);

    void deleteByContentId(Integer contentId);
}
