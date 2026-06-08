package org.example.patientmicroservice.controller;


import org.example.patientmicroservice.model.Patient;
import org.example.patientmicroservice.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable String id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email) {

        if (email != null) {
            return ResponseEntity.ok(patientService.getPatientByEmail(email));
        }
        if (name != null) {
            String[] parts = name.trim().split("\\s+", 2);
            if (parts.length == 2) {
                return ResponseEntity.ok(patientService.getPatientByName(parts[0], parts[1]));
            }
            return ResponseEntity.ok(patientService.getPatientByLastName(parts[0]));
        }
        if (lastName != null) {
            return ResponseEntity.ok(patientService.getPatientByLastName(lastName));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping
    public ResponseEntity<Patient> addPatient(@Valid @RequestBody Patient patient) {
        Patient saved = patientService.addPatient(patient);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(
            @PathVariable String id,
            @Valid @RequestBody Patient patient) {
        Patient updated = patientService.updatePatient(id, patient);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable String id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
