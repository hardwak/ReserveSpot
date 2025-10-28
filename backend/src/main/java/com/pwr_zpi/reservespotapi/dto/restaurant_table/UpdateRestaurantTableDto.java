package com.pwr_zpi.reservespotapi.dto.restaurant_table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdateRestaurantTableDto {
    private Integer tableNumber;
    
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
    
    private String locationInRestaurant;
}
