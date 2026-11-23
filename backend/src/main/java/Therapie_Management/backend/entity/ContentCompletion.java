package Therapie_Management.backend.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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

/** Ein Patient hat einen Tipp ausprobiert bzw. eine Uebung durchgefuehrt (je Paar nur einmal). */
@Entity
@Table(
    name = "content_completions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"patient_id", "content_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class ContentCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @CreationTimestamp
    @Column(name = "abgeschlossen_am", updatable = false)
    private LocalDateTime abgeschlossenAm;

    public ContentCompletion(Patient patient, Content content) {
        this.patient = patient;
        this.content = content;
    }
}
