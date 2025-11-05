package com.pwr_zpi.reservespotapi.entities.users.dto;

import com.pwr_zpi.reservespotapi.entities.users.AuthProvider;
import com.pwr_zpi.reservespotapi.entities.users.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateUserDto {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String passwordHash;

    private String phoneNumber;

    @NotNull(message = "Role is required")
    private Role role;

    private String oauthProviderId;

    private AuthProvider provider;

    private Long pictureId;
}

