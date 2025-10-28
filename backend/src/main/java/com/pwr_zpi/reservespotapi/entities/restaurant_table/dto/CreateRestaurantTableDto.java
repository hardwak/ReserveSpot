package com.pwr_zpi.reservespotapi.entities.restaurant_table.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateRestaurantTableDto {
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
    
    private Integer tableNumber;
    
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
    
    private String locationInRestaurant;
}
