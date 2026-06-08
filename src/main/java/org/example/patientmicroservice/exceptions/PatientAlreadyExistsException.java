package org.example.patientmicroservice.exceptions;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class PatientAlreadyExistsException extends RuntimeException {
    public PatientAlreadyExistsException(String message) { super(message); }
}
