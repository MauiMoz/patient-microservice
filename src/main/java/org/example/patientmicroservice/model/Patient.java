package org.example.patientmicroservice.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "patients")
public class Patient {

    @Id
    private String id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid contact number format")
    private String contactNumber;

    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email address format")
    private String emailAddress;

    @NotNull(message = "Gender is required")
    private Gender gender;

    public enum Gender {
        MALE, FEMALE, OTHER
    }
}
