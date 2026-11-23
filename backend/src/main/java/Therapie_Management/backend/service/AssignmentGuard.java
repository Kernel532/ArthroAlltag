package Therapie_Management.backend.service;

import org.springframework.stereotype.Component;

import Therapie_Management.backend.entity.Patient;
import Therapie_Management.backend.exception.ForbiddenException;

/** Stellt sicher, dass ein Patient dem handelnden Therapeuten zugeordnet ist. */
@Component
public class AssignmentGuard {

    public void ensureAssigned(String therapeutId, Patient patient) {
        if (patient.getTherapeut() == null || !patient.getTherapeut().getId().equals(therapeutId)) {
            throw new ForbiddenException("Patient ist nicht dir zugeordnet");
        }
    }
}
