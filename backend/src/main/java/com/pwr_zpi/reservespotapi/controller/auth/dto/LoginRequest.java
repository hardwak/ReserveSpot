package com.pwr_zpi.reservespotapi.controller.auth.dto;

public record LoginRequest(
        String email,
        String password) {
}
