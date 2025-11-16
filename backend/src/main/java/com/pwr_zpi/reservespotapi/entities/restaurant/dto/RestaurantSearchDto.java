package com.pwr_zpi.reservespotapi.entities.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RestaurantSearchDto {
    private String query;
    private String city;
    private List<Long> tagIds;
    private Double minRating;
    private Double maxRating;
}

