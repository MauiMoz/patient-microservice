package org.example.patientmicroservice.service;


import org.example.patientmicroservice.exceptions.PatientAlreadyExistsException;
import org.example.patientmicroservice.exceptions.PatientNotFoundException;
import org.example.patientmicroservice.model.Patient;
import org.example.patientmicroservice.repository.IPatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService implements IPatientService {

    private final IPatientRepository patientRepository;

    @Override
    public List<Patient> getAllPatients() {
        log.info("Fetching all patients");
        return patientRepository.findAll();
    }

    @Override
    public Patient getPatientById(String id) {
        log.info("Fetching patient with id: {}", id);
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient with id: " + id + " not found"));
    }

    @Override
    public List<Patient> getPatientByLastName(String lastName) {
        log.info("Fetching patients with last name: {}", lastName);
        return patientRepository.findByLastNameIgnoreCase(lastName);
    }

    @Override
    public List<Patient> getPatientByName(String firstName, String lastName) {
        log.info("Fetching patients with name: {} {}", firstName, lastName);
        return patientRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName);
    }

    @Override
    public Patient getPatientByEmail(String email) {
        log.info("Fetching patient with email address: {}", email);
        return patientRepository.findByEmailAddressIgnoreCase(email)
                .orElseThrow(() -> new PatientNotFoundException("Patient with Email address: "
                        + email + " not found"));
    }

    @Override
    public Patient addPatient(Patient patient) {
        log.info("Adding new patient: {} {}", patient.getFirstName(), patient.getLastName());
        patient.setId(null);
        if (patientRepository.findByEmailAddressIgnoreCase(patient.getEmailAddress()).isPresent()) {
            throw new PatientAlreadyExistsException("Patient with email " +
                    patient.getEmailAddress() + " already exists");
        }
        else if (patientRepository.findByContactNumber(patient.getContactNumber()).isPresent()) {
            throw new PatientAlreadyExistsException("Patient with contact number: " +
                    patient.getContactNumber() + " already exists");
        }
        return patientRepository.save(patient);
    }

    @Override
    public Patient updatePatient(String id, Patient patient) {
        log.info("Updating patient with id: {}", id);
        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException("Patient with id: " + id + " not found");
        }
        patient.setId(id);
        return patientRepository.save(patient);
    }

    @Override
    public void deletePatient(String id) {
        log.info("Deleting patient with id: {}", id);
        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException("Patient with id: " + id + " not found");
        }
        patientRepository.deleteById(id);
    }
}
