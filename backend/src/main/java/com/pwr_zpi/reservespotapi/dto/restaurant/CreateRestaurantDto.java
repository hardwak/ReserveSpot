package com.pwr_zpi.reservespotapi.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateRestaurantDto {
    @NotNull(message = "Owner ID is required")
    private Long ownerId;
    
    @NotBlank(message = "Restaurant name is required")
    private String name;
    
    private String address;
    private String city;
    private String description;
    private String openingHours;
    private String pic;
}
