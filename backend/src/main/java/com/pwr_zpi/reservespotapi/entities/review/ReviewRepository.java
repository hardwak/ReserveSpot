package com.pwr_zpi.reservespotapi.entities.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserId(Long userId);
    List<Review> findByRestaurantId(Long restaurantId);
    List<Review> findByRating(Integer rating);
    List<Review> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    boolean existsByUserIdAndRestaurantId(Long userId, Long restaurantId);
    Optional<Review> findByIdAndUserId(Long id, Long userId);
}
