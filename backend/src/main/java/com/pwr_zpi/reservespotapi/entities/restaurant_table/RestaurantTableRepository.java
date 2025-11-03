package com.pwr_zpi.reservespotapi.entities.restaurant_table;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findByRestaurantId(Long restaurantId);
    List<RestaurantTable> findByCapacity(Integer capacity);
}
