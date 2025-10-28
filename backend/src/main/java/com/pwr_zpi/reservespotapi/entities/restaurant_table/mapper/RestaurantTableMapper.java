package com.pwr_zpi.reservespotapi.entities.restaurant_table.mapper;

import com.pwr_zpi.reservespotapi.entities.restaurant_table.dto.CreateRestaurantTableDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.dto.RestaurantTableDto;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.dto.UpdateRestaurantTableDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTable;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RestaurantTableMapper {

    public RestaurantTableDto toDto(RestaurantTable table) {
        if (table == null) {
            return null;
        }

        return RestaurantTableDto.builder()
                .id(table.getId())
                .restaurantId(table.getRestaurant() != null ? table.getRestaurant().getId() : null)
                .tableNumber(table.getTableNumber())
                .capacity(table.getCapacity())
                .locationInRestaurant(table.getLocationInRestaurant())
                .reservationIds(table.getReservations() != null ? 
                    table.getReservations().stream().map(reservation -> reservation.getId()).collect(Collectors.toSet()) : null)
                .build();
    }

    public RestaurantTable toEntity(CreateRestaurantTableDto dto) {
        if (dto == null) {
            return null;
        }

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(dto.getTableNumber())
                .capacity(dto.getCapacity())
                .locationInRestaurant(dto.getLocationInRestaurant())
                .build();

        // Set restaurant if provided
        if (dto.getRestaurantId() != null) {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(dto.getRestaurantId());
            table.setRestaurant(restaurant);
        }

        return table;
    }

    public void updateEntity(UpdateRestaurantTableDto dto, RestaurantTable table) {
        if (dto == null || table == null) {
            return;
        }

        if (dto.getTableNumber() != null) {
            table.setTableNumber(dto.getTableNumber());
        }
        if (dto.getCapacity() != null) {
            table.setCapacity(dto.getCapacity());
        }
        if (dto.getLocationInRestaurant() != null) {
            table.setLocationInRestaurant(dto.getLocationInRestaurant());
        }
    }
}
