package Therapie_Management.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import Therapie_Management.backend.entity.Category;
import Therapie_Management.backend.entity.Content;
import Therapie_Management.backend.entity.Module;
import Therapie_Management.backend.entity.Patient;
import Therapie_Management.backend.entity.PatientFavorite;
import Therapie_Management.backend.entity.Therapeut;

@DataJpaTest
class ContentRepositorySearchTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ContentRepository contentRepository;

    private Content tipp(String titel, String beschreibung, Category kategorie, Module modul, Therapeut therapeut) {
        Content content = new Content();
        content.setTitel(titel);
        content.setBeschreibung(beschreibung);
        content.setKategorie(kategorie);
        content.setModul(modul);
        content.setErstelltDurch(therapeut);
        return entityManager.persist(content);
    }

    @Test
    void searchVisible_beachtetModulSucheFavoritenUndSeiten() {
        Patient patient = entityManager.persist(
            new Patient("v110098", "Test", "Patientin", LocalDate.of(1990, 1, 1), "hash", "Diagnose", null));
        Therapeut therapeut = entityManager.persist(
            new Therapeut("v220098", "Test", "Therapeut", LocalDate.of(1985, 1, 1), "hash", "t@example.com", "Test"));
        Category kueche = entityManager.persist(new Category("TestKueche"));
        Category garten = entityManager.persist(new Category("TestGarten"));
        Module offen = entityManager.persist(new Module(9001, "Offen", 1, null));
        Module gesperrt = entityManager.persist(new Module(9002, "Gesperrt", 2, null));

        Content messer = tipp("Messer", "Ergonomisches Schneiden", kueche, offen, therapeut);
        tipp("Gabel", "Besteck mit dickem Griff", kueche, offen, therapeut);
        tipp("Harke", "Gartenarbeit ohne Schmerzen", garten, offen, therapeut);
        tipp("Geheim", "Messer im gesperrten Modul", kueche, gesperrt, therapeut);
        entityManager.persist(new PatientFavorite(patient, messer));
        entityManager.flush();

        Set<Integer> module = Set.of(offen.getId());
        PageRequest zwei = PageRequest.of(0, 2, Sort.by("titel", "id"));

        var alle = contentRepository.searchVisible(module, null, "%%", false, "v110098", zwei);
        assertThat(alle.getTotalElements()).isEqualTo(3);
        assertThat(alle.getContent()).hasSize(2);

        var suche = contentRepository.searchVisible(module, null, "%schneiden%", false, "v110098", zwei);
        assertThat(suche.getContent()).extracting(Content::getTitel).containsExactly("Messer");

        var kategorie = contentRepository.searchVisible(module, garten.getId(), "%%", false, "v110098", zwei);
        assertThat(kategorie.getContent()).extracting(Content::getTitel).containsExactly("Harke");

        var favoriten = contentRepository.searchVisible(module, null, "%%", true, "v110098", zwei);
        assertThat(favoriten.getContent()).extracting(Content::getTitel).containsExactly("Messer");
    }
}
