package Therapie_Management.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Selbstregistrierung: legt immer einen Patienten an, die Rolle kommt nie vom Client. */
public record RegisterRequest(
    @NotBlank(message = "Vorname darf nicht leer sein") @Size(max = 255) String vorname,
    @NotBlank(message = "Nachname darf nicht leer sein") @Size(max = 255) String nachname,
    @Pattern(regexp = "^[A-Za-z0-9_]{3,10}$",
        message = "Benutzername muss 3-10 Zeichen lang sein (Buchstaben, Ziffern, _)") String username,
    @NotBlank(message = "Passwort darf nicht leer sein")
    @Size(min = 6, max = 72, message = "Passwort muss 6-72 Zeichen lang sein") String password
) {
}
