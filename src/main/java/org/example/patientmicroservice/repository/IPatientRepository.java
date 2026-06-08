package org.example.patientmicroservice.repository;


import org.example.patientmicroservice.model.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPatientRepository extends MongoRepository<Patient, String> {

    List<Patient> findByLastNameIgnoreCase(String lastName);

    List<Patient> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    Optional<Patient> findByEmailAddressIgnoreCase(String emailAddress);

    Optional<Patient> findByContactNumber(String contactNumber);
}
