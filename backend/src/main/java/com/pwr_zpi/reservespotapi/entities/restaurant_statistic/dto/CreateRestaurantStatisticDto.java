package com.pwr_zpi.reservespotapi.entities.restaurant_statistic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateRestaurantStatisticDto {
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
    
    @Min(value = 0, message = "Hour must be between 0 and 23")
    @Max(value = 23, message = "Hour must be between 0 and 23")
    private Integer hourOfDay;
    
    private Double averageOccupancy;
    
    private LocalDate date;
}
