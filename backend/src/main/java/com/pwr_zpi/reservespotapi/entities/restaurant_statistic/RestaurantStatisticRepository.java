package com.pwr_zpi.reservespotapi.entities.restaurant_statistic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RestaurantStatisticRepository extends JpaRepository<RestaurantStatistic, Long> {
    List<RestaurantStatistic> findByRestaurantId(Long restaurantId);
    List<RestaurantStatistic> findByDate(LocalDate date);
}
