package com.pwr_zpi.reservespotapi.controller.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotBlank
        String name,
        @Email
        String email,
        @NotBlank
        String password,
        @NotNull
        String role) {
}