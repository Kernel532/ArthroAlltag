package Therapie_Management.backend.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patients")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@NoArgsConstructor
public class Patient extends User {

    private String diagnostik;

    private String notizen;

    /** Zustaendiger Therapeut; null, solange der Patient noch niemandem zugeordnet ist. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapeut_id")
    private Therapeut therapeut;

    public Patient(String id, String vorname, String nachname, LocalDate geburtsdatum,
                   String passwortHash, String diagnostik, String notizen) {
        super(id, vorname, nachname, geburtsdatum, passwortHash, Role.PATIENT);
        this.diagnostik = diagnostik;
        this.notizen = notizen;
    }
}
