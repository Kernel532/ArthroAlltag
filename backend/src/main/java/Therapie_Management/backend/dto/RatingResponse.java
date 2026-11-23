package Therapie_Management.backend.dto;

/** Eigene Bewertung eines Patienten fuer einen Tipp. */
public record RatingResponse(
    Integer contentId,
    int sterne
) {
}
