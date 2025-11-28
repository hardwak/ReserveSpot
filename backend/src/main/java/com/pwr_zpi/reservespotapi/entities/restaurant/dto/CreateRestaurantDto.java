package com.pwr_zpi.reservespotapi.entities.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateRestaurantDto {
    private Long ownerId;
    
    @NotBlank(message = "Restaurant name is required")
    private String name;
    
    private String address;
    private String city;
    private String description;
    private String openingHours; // JSON string from form, will be parsed to Map in mapper
    private Double latitude;
    private Double longitude;
    private String pic;
    private Set<Long> tagIds;
}
