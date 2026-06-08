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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@DisplayName("PatientController Unit Tests")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    private ObjectMapper objectMapper;
    private Patient samplePatient;
    private Patient otherPatient;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules();

        samplePatient = Patient.builder()
                .id("patient-001")
                .firstName("Alice")
                .lastName("Müller")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .contactNumber("+4366412345678")
                .emailAddress("alice.mueller@gmail.com")
                .gender(Patient.Gender.FEMALE)
                .build();

        otherPatient = Patient.builder()
                .id("patient-002")
                .firstName("Bob")
                .lastName("Schmidt")
                .dateOfBirth(LocalDate.of(1990, 7, 22))
                .contactNumber("+4369912345678")
                .emailAddress("bob.schmidt@gmail.com")
                .gender(Patient.Gender.MALE)
                .build();
    }

    // Get all patients
    @Test
    @DisplayName("GET /api/patients returns 200 with list of patients")
    void getAllPatients_returnsOkWithList() throws Exception {
        when(patientService.getAllPatients()).thenReturn(List.of(samplePatient, otherPatient));

        mockMvc.perform(get("/api/patients")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("patient-001")))
                .andExpect(jsonPath("$[0].firstName", is("Alice")))
                .andExpect(jsonPath("$[1].id", is("patient-002")))
                .andExpect(jsonPath("$[1].firstName", is("Bob")));

        verify(patientService, times(1)).getAllPatients();
    }

    @Test
    @DisplayName("GET /api/patients returns 200 with empty list when no patients exist")
    void getAllPatients_returnsEmptyList() throws Exception {
        when(patientService.getAllPatients()).thenReturn(List.of());

        mockMvc.perform(get("/api/patients")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // GET patient by ID
    @Test
    @DisplayName("GET /api/patients/{id} returns 200 with patient when found")
    void getPatientById_returnsOkWithPatient() throws Exception {
        when(patientService.getPatientById("patient-001")).thenReturn(samplePatient);

        mockMvc.perform(get("/api/patients/patient-001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("patient-001")))
                .andExpect(jsonPath("$.firstName", is("Alice")))
                .andExpect(jsonPath("$.lastName", is("Müller")))
                .andExpect(jsonPath("$.emailAddress", is("alice.mueller@gmail.com")))
                .andExpect(jsonPath("$.gender", is("FEMALE")));
    }

    @Test
    @DisplayName("GET /api/patients/{id} returns 404 when patient not found")
    void getPatientById_returnsNotFound() throws Exception {
        when(patientService.getPatientById("nonexistent"))
                .thenThrow(new PatientNotFoundException("Patient not found with id: nonexistent"));

        mockMvc.perform(get("/api/patients/nonexistent")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Patient not found with id: nonexistent")));
    }

    // Add patient
    @Test
    @DisplayName("POST /api/patients returns 201 with created patient")
    void addPatient_returnsCreated() throws Exception {
        when(patientService.addPatient(any(Patient.class))).thenReturn(samplePatient);

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePatient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("patient-001")))
                .andExpect(jsonPath("$.firstName", is("Alice")));

        verify(patientService, times(1)).addPatient(any(Patient.class));
    }

    @Test
    @DisplayName("POST /api/patients returns 409 when email already exists")
    void addPatient_returnsConflictWhenEmailExists() throws Exception {
        when(patientService.addPatient(any(Patient.class)))
                .thenThrow(new PatientAlreadyExistsException("Patient with email alice.mueller@gmail.com already exists"));

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePatient)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("alice.mueller@gmail.com")));
    }

    @Test
    @DisplayName("POST /api/patients returns 400 when firstName is blank")
    void addPatient_returnsBadRequestWhenFirstNameBlank() throws Exception {
        samplePatient.setFirstName("");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePatient)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.firstName", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/patients returns 400 when email is invalid")
    void addPatient_returnsBadRequestWhenEmailInvalid() throws Exception {
        samplePatient.setEmailAddress("not-an-email");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePatient)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.emailAddress", notNullValue()));
    }

    // Update patient
    @Test
    @DisplayName("PUT /api/patients/{id} returns 200 with updated patient")
    void updatePatient_returnsOkWithUpdated() throws Exception {
        samplePatient.setEmailAddress("alice.new@example.com");
        when(patientService.updatePatient(eq("patient-001"), any(Patient.class)))
                .thenReturn(samplePatient);

        mockMvc.perform(put("/api/patients/patient-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePatient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailAddress", is("alice.new@example.com")));
    }

    @Test
    @DisplayName("PUT /api/patients/{id} returns 404 when patient not found")
    void updatePatient_returnsNotFound() throws Exception {
        when(patientService.updatePatient(eq("nonexistent"), any(Patient.class)))
                .thenThrow(new PatientNotFoundException("Patient not found with id: nonexistent"));

        mockMvc.perform(put("/api/patients/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(samplePatient)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    // Delete patient
    @Test
    @DisplayName("DELETE /api/patients/{id} returns 204 when successfully deleted")
    void deletePatient_returnsNoContent() throws Exception {
        doNothing().when(patientService).deletePatient("patient-001");

        mockMvc.perform(delete("/api/patients/patient-001"))
                .andExpect(status().isNoContent());

        verify(patientService, times(1)).deletePatient("patient-001");
    }

    @Test
    @DisplayName("DELETE /api/patients/{id} returns 404 when patient not found")
    void deletePatient_returnsNotFound() throws Exception {
        doThrow(new PatientNotFoundException("Patient not found with id: nonexistent"))
                .when(patientService).deletePatient("nonexistent");

        mockMvc.perform(delete("/api/patients/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
