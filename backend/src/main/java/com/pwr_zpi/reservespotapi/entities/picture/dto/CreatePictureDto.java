package com.pwr_zpi.reservespotapi.entities.picture.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreatePictureDto {
    @NotBlank(message = "Picture URL is required")
    private String url;
    
    private String description;
}
