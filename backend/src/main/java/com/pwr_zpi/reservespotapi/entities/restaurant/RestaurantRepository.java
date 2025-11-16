package com.pwr_zpi.reservespotapi.entities.restaurant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, JpaSpecificationExecutor<Restaurant> {
    List<Restaurant> findByOwnerId(Long ownerId);
    List<Restaurant> findByCityIgnoreCase(String city);
    List<Restaurant> findByNameContainingIgnoreCase(String name);
    Page<Restaurant> findByNameContainingIgnoreCase(String name, Pageable pageable);
}