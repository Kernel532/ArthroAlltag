package Therapie_Management.backend.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Sterne-Bewertung (1-5) eines Patienten fuer einen Tipp/eine Uebung; je Patient und Tipp genau eine. */
@Entity
@Table(
    name = "content_ratings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"patient_id", "content_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class ContentRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(nullable = false)
    private int sterne;

    @UpdateTimestamp
    @Column(name = "bewertet_am")
    private LocalDateTime bewertetAm;

    public ContentRating(Patient patient, Content content, int sterne) {
        this.patient = patient;
        this.content = content;
        this.sterne = sterne;
    }
}
