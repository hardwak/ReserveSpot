package com.pwr_zpi.reservespotapi.controller.auth.dto;

import jakarta.validation.constraints.NotNull;

public record GoogleTokenRequest(@NotNull String googleToken) {
}
