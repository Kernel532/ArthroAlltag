package Therapie_Management.backend.exception;

/** Ressource existiert bereits (z.B. Benutzername vergeben) -> 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
