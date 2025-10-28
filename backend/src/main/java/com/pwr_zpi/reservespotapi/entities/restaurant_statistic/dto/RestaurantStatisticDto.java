package com.pwr_zpi.reservespotapi.entities.restaurant_statistic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RestaurantStatisticDto {
    private Long id;
    private Long restaurantId;
    private Integer hourOfDay;
    private Double averageOccupancy;
    private LocalDate date;
}
