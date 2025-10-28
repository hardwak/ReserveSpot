package com.pwr_zpi.reservespotapi.dto.tag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateTagDto {
    @NotBlank(message = "Tag name is required")
    private String name;
}
