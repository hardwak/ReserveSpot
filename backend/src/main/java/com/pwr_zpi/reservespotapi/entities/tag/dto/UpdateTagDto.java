package com.pwr_zpi.reservespotapi.entities.tag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdateTagDto {
    @NotBlank(message = "Tag name is required")
    private String name;
}
