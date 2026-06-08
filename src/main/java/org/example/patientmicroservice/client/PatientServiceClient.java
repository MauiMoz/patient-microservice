package org.example.patientmicroservice.client;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.patientmicroservice.model.Patient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

// RESTful service client made to run all the patient service functionalities
public class PatientServiceClient {

    private static final String BASE_URL = "http://localhost:8080/api/patients";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;

    public PatientServiceClient() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.findAndRegisterModules();
    }

    public List<Patient> getAllPatients() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("GET /api/patients -> " + response.statusCode());
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    public Patient getPatientById(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("GET /api/patients/" + id + " -> " + response.statusCode());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Patient.class);
        }
        throw new RuntimeException("Patient not found: " + id);
    }

    public Patient addPatient(Patient patient) throws Exception {
        String body = objectMapper.writeValueAsString(patient);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("POST /api/patients -> " + response.statusCode());
        System.out.println("Response body: " + response.body());
        return objectMapper.readValue(response.body(), Patient.class);
    }

    public Patient updatePatient(String id, Patient patient) throws Exception {
        String body = objectMapper.writeValueAsString(patient);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("PUT /api/patients/" + id + " -> " + response.statusCode());
        return objectMapper.readValue(response.body(), Patient.class);
    }

    public void deletePatient(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("DELETE /api/patients/" + id + " -> " + response.statusCode());
    }

    public static void main(String[] args) throws Exception {
        PatientServiceClient client = new PatientServiceClient();

        System.out.println("\n=== Patient Microservice Client Demo ===\n");

        // Add patients
        System.out.println("--- Adding patients ---");
        Patient p1 = Patient.builder()
                .firstName("Alice")
                .lastName("Müller")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .contactNumber("+4366412345678")
                .emailAddress("alice.mueller@gmail.com")
                .gender(Patient.Gender.FEMALE)
                .build();

        Patient p2 = Patient.builder()
                .firstName("Bob")
                .lastName("Schmidt")
                .dateOfBirth(LocalDate.of(1990, 7, 22))
                .contactNumber("+4366487654321")
                .emailAddress("bob.schmidt@gmail.com")
                .gender(Patient.Gender.MALE)
                .build();

        Patient saved1 = client.addPatient(p1);
        System.out.println("  Created: " + saved1.getId() + " – " + saved1.getFirstName() + " " + saved1.getLastName());

        Patient saved2 = client.addPatient(p2);
        System.out.println("  Created: " + saved2.getId() + " – " + saved2.getFirstName() + " " + saved2.getLastName());

        // Get all patients
        System.out.println("\n--- All patients ---");
        List<Patient> all = client.getAllPatients();
        all.forEach(p -> System.out.println("  " + p.getId() + " | " + p.getFirstName() + " " + p.getLastName()));

        // Get single patient
        System.out.println("\n--- Get patient by ID ---");
        Patient fetched = client.getPatientById(saved1.getId());
        System.out.println("  Fetched: " + fetched.getFirstName() + " " + fetched.getLastName()
                + ", date of birth: " + fetched.getDateOfBirth());

        // Update patient
        System.out.println("\n--- Update patient ---");
        saved1.setContactNumber("+4366413243546");
        saved1.setEmailAddress("alice.new@gmail.com");
        Patient updated = client.updatePatient(saved1.getId(), saved1);
        System.out.println("  Updated email: " + updated.getEmailAddress());
        System.out.println("  Updated phone: " + updated.getContactNumber());

        // Delete patient
        System.out.println("\n--- Delete patient ---");
        client.deletePatient(saved2.getId());

        // Verify deletion
        System.out.println("\n--- Remaining patients ---");
        List<Patient> remaining = client.getAllPatients();
        remaining.forEach(p -> System.out.println("  " + p.getId() + " | " + p.getFirstName() + " " + p.getLastName()));

        System.out.println("\n=== Demo complete ===");
    }
}
