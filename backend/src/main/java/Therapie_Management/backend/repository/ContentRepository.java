package Therapie_Management.backend.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import Therapie_Management.backend.entity.Content;

public interface ContentRepository extends JpaRepository<Content, Integer> {

    Page<Content> findByKategorieId(Integer kategorieId, Pageable pageable);

    /** Sichtbare Tipps eines Patienten; searchPattern ist ein kleingeschriebenes LIKE-Muster (z.B. "%knie%"). */
    @Query("""
        select c from Content c
        where c.modul.id in :modulIds
          and (:kategorieId is null or c.kategorie.id = :kategorieId)
          and (lower(c.titel) like :searchPattern or lower(c.beschreibung) like :searchPattern)
          and (:favoritesOnly = false or c.id in
               (select f.tipp.id from PatientFavorite f where f.patient.id = :patientId))
        """)
    Page<Content> searchVisible(@Param("modulIds") Collection<Integer> modulIds,
                                @Param("kategorieId") Integer kategorieId,
                                @Param("searchPattern") String searchPattern,
                                @Param("favoritesOnly") boolean favoritesOnly,
                                @Param("patientId") String patientId,
                                Pageable pageable);

    List<Content> findByModulIdIn(Collection<Integer> modulIds);

    List<Content> findByModulIdInAndKategorieId(Collection<Integer> modulIds, Integer kategorieId);
}
