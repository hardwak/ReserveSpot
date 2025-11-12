package com.pwr_zpi.reservespotapi.entities.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdateProfileDto {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 255, message = "Name must be shorter than 255 characters")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    private String email;

    @Pattern(
            regexp = "^\\+?[0-9\\s-]{7,15}$",
            message = "Phone number must contain 7-15 digits and may start with '+'"
    )
    private String phoneNumber;
}

