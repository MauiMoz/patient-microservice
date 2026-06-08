package org.example.patientmicroservice.service;


import org.example.patientmicroservice.model.Patient;

import java.util.List;

public interface IPatientService {

    List<Patient> getAllPatients();

    Patient getPatientById(String id);

    List<Patient> getPatientByLastName(String lastName);

    List<Patient> getPatientByName(String firstName, String lastName);

    Patient getPatientByEmail(String email);

    Patient addPatient(Patient patient);

    Patient updatePatient(String id, Patient patient);

    void deletePatient(String id);
}
