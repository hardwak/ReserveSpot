package com.pwr_zpi.reservespotapi.entities.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdateRestaurantDto {
    private String name;
    private String address;
    private String city;
    private String description;
    private Map<String, String> openingHours;
    private Double latitude;
    private Double longitude;
    private String pic;
}
