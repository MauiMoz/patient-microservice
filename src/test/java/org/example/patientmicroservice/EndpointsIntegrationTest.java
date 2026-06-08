package org.example.patientmicroservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.patientmicroservice.controller.PatientController;
import org.example.patientmicroservice.exceptions.PatientAlreadyExistsException;
import org.example.patientmicroservice.exceptions.PatientNotFoundException;
import org.example.patientmicroservice.model.Patient;
import org.example.patientmicroservice.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(PatientController.class)
@DisplayName("PatientController Endpoint Tests")
class PatientEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    private ObjectMapper objectMapper;
    private Patient patient;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        patient = Patient.builder()
                .id("1")
                .firstName("Alice")
                .lastName("Müller")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .contactNumber("+4366412345678")
                .emailAddress("alice.mueller@gmail.com")
                .gender(Patient.Gender.FEMALE)
                .build();
    }

    // Get all patients
    @Test
    @DisplayName("GET /api/patients returns 200 with list of patients")
    void shouldReturnAllPatients() throws Exception {
        when(patientService.getAllPatients()).thenReturn(List.of(patient));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName", is("Alice")));
    }

    // Get patient by ID
    @Test
    @DisplayName("GET /api/patients/{id} returns 200 with patient when found")
    void shouldReturnPatientById() throws Exception {
        when(patientService.getPatientById("1")).thenReturn(patient);

        mockMvc.perform(get("/api/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Alice")));
    }

    @Test
    @DisplayName("GET /api/patients/{id} returns 404 when patient not found")
    void shouldReturn404WhenPatientNotFound() throws Exception {
        when(patientService.getPatientById("999"))
                .thenThrow(new PatientNotFoundException("Patient with id: 999 not found"));

        mockMvc.perform(get("/api/patients/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("999")));
    }

    // Search for patient
    @Test
    @DisplayName("SEARCH patient by email address")
    void shouldSearchByEmail() throws Exception {
        when(patientService.getPatientByEmail("alice.mueller@gmail.com")).thenReturn(patient);

        mockMvc.perform(get("/api/patients/search")
                        .param("email", "alice.mueller@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailAddress", is("alice.mueller@gmail.com")));
    }

    @Test
    @DisplayName("SEARCH patient by name")
    void shouldSearchByFullName() throws Exception {
        when(patientService.getPatientByName("Alice", "Müller")).thenReturn(List.of(patient));

        mockMvc.perform(get("/api/patients/search")
                        .param("name", "Alice Müller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName", is("Alice")));
    }

    @Test
    @DisplayName("SEARCH patient by last name")
    void shouldSearchByLastName() throws Exception {
        when(patientService.getPatientByLastName("Müller")).thenReturn(List.of(patient));

        mockMvc.perform(get("/api/patients/search")
                        .param("lastName", "Müller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lastName", is("Müller")));
    }

    // Create new patient
    @Test
    @DisplayName("POST /api/patients returns 201 with created patient")
    void shouldCreatePatient() throws Exception {
        when(patientService.addPatient(ArgumentMatchers.any(Patient.class))).thenReturn(patient);

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is("Alice")));
    }

    @Test
    @DisplayName("POST /api/patients returns 409 when email already exists")
    void shouldReturnConflictWhenEmailExists() throws Exception {
        when(patientService.addPatient(ArgumentMatchers.any(Patient.class)))
                .thenThrow(new PatientAlreadyExistsException("Patient with email already exists"));

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patient)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    // Update patient
    @Test
    @DisplayName("PUT /api/patients/{id} returns 200 with updated patient")
    void shouldUpdatePatient() throws Exception {
        when(patientService.updatePatient(eq("1"), ArgumentMatchers.any(Patient.class))).thenReturn(patient);

        mockMvc.perform(put("/api/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Alice")));
    }

    @Test
    @DisplayName("PUT /api/patients/{id} returns 404 when patient not found")
    void shouldReturn404WhenUpdatingNonexistentPatient() throws Exception {
        when(patientService.updatePatient(eq("999"), ArgumentMatchers.any(Patient.class)))
                .thenThrow(new PatientNotFoundException("Patient with id: 999 not found"));

        mockMvc.perform(put("/api/patients/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patient)))
                .andExpect(status().isNotFound());
    }

    // Delete patient
    @Test
    @DisplayName("DELETE /api/patients/{id} returns 204 when successfully deleted")
    void shouldDeletePatient() throws Exception {
        doNothing().when(patientService).deletePatient("1");

        mockMvc.perform(delete("/api/patients/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/patients/{id} returns 404 when patient not found")
    void shouldReturn404WhenDeletingNonexistentPatient() throws Exception {
        doThrow(new PatientNotFoundException("Patient with id: 999 not found"))
                .when(patientService).deletePatient("999");

        mockMvc.perform(delete("/api/patients/999"))
                .andExpect(status().isNotFound());
    }
}
