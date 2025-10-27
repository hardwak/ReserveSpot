package com.pwr_zpi.reservespotapi.entities.restaurant_statistic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantStatisticRepository extends JpaRepository<RestaurantStatistic, Long> {
}
