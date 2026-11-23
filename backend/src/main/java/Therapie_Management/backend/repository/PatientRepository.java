package Therapie_Management.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import Therapie_Management.backend.entity.Patient;

public interface PatientRepository extends JpaRepository<Patient, String> {

    Page<Patient> findByTherapeutId(String therapeutId, Pageable pageable);

    Page<Patient> findByTherapeutIsNull(Pageable pageable);
}
