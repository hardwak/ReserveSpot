package com.pwr_zpi.reservespotapi.entities.restaurant_statistic.mapper;

import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.dto.CreateRestaurantStatisticDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.dto.RestaurantStatisticDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.dto.UpdateRestaurantStatisticDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.RestaurantStatistic;
import org.springframework.stereotype.Component;

@Component
public class RestaurantStatisticMapper {

    public RestaurantStatisticDto toDto(RestaurantStatistic statistic) {
        if (statistic == null) {
            return null;
        }

        return RestaurantStatisticDto.builder()
                .id(statistic.getId())
                .restaurantId(statistic.getRestaurant() != null ? statistic.getRestaurant().getId() : null)
                .hourOfDay(statistic.getHourOfDay())
                .averageOccupancy(statistic.getAverageOccupancy())
                .date(statistic.getDate())
                .build();
    }

    public RestaurantStatistic toEntity(CreateRestaurantStatisticDto dto) {
        if (dto == null) {
            return null;
        }

        RestaurantStatistic statistic = RestaurantStatistic.builder()
                .hourOfDay(dto.getHourOfDay())
                .averageOccupancy(dto.getAverageOccupancy())
                .date(dto.getDate())
                .build();

        // Set restaurant if provided
        if (dto.getRestaurantId() != null) {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(dto.getRestaurantId());
            statistic.setRestaurant(restaurant);
        }

        return statistic;
    }

    public void updateEntity(UpdateRestaurantStatisticDto dto, RestaurantStatistic statistic) {
        if (dto == null || statistic == null) {
            return;
        }

        if (dto.getHourOfDay() != null) {
            statistic.setHourOfDay(dto.getHourOfDay());
        }
        if (dto.getAverageOccupancy() != null) {
            statistic.setAverageOccupancy(dto.getAverageOccupancy());
        }
        if (dto.getDate() != null) {
            statistic.setDate(dto.getDate());
        }
    }
}
