package com.pwr_zpi.reservespotapi.entities.restaurant_table.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RestaurantTableDto {
    private Long id;
    private Long restaurantId;
    private Integer tableNumber;
    private Integer capacity;
    private String locationInRestaurant;
    private Set<Long> reservationIds;
}
