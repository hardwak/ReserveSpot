package com.pwr_zpi.reservespotapi.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RestaurantDto {
    private Long id;
    private Long ownerId;
    private String name;
    private String address;
    private String city;
    private String description;
    private String openingHours;
    private Double averageRating;
    private String pic;
    private Set<Long> tableIds;
    private Set<Long> reviewIds;
    private Set<Long> aiAnalysisIds;
    private Set<Long> statisticIds;
    private Set<Long> tagIds;
    private Set<Long> pictureIds;
}
