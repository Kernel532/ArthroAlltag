package Therapie_Management.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RatingRequest(
    @NotNull(message = "sterne muss angegeben werden")
    @Min(value = 1, message = "sterne muss zwischen 1 und 5 liegen")
    @Max(value = 5, message = "sterne muss zwischen 1 und 5 liegen")
    Integer sterne
) {
}
