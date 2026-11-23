package Therapie_Management.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Fortschritt eines Patienten: wie viele Tipps/Uebungen es gibt, wie viele er schon
 * abgeschlossen hat (aufgeschluesselt nach Kategorie) und welche das genau sind.
 */
public record ProgressResponse(
    String patientId,
    String vorname,
    String nachname,
    int gesamt,
    int abgeschlossen,
    List<CategoryProgress> kategorien,
    List<CompletedContent> abgeschlosseneInhalte
) {

    public record CategoryProgress(String kategorie, int gesamt, int abgeschlossen) {
    }

    public record CompletedContent(Integer contentId, String titel, String kategorie, LocalDateTime abgeschlossenAm) {
    }
}
