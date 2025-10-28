package com.pwr_zpi.reservespotapi.controller.review;

import com.pwr_zpi.reservespotapi.dto.review.CreateReviewDto;
import com.pwr_zpi.reservespotapi.dto.review.ReviewDto;
import com.pwr_zpi.reservespotapi.dto.review.UpdateReviewDto;
import com.pwr_zpi.reservespotapi.service.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        List<ReviewDto> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable Long id) {
        Optional<ReviewDto> review = reviewService.getReviewById(id);
        return review.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByUser(@PathVariable Long userId) {
        List<ReviewDto> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByRestaurant(@PathVariable Long restaurantId) {
        List<ReviewDto> reviews = reviewService.getReviewsByRestaurantId(restaurantId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/rating/{rating}")
    public ResponseEntity<List<ReviewDto>> getReviewsByRating(@PathVariable Integer rating) {
        List<ReviewDto> reviews = reviewService.getReviewsByRating(rating);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ReviewDto>> getReviewsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<ReviewDto> reviews = reviewService.getReviewsByDateRange(startDate, endDate);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody CreateReviewDto createDto) {
        ReviewDto createdReview = reviewService.createReview(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable Long id, 
                                                 @Valid @RequestBody UpdateReviewDto updateDto) {
        Optional<ReviewDto> updatedReview = reviewService.updateReview(id, updateDto);
        return updatedReview.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        boolean deleted = reviewService.deleteReview(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getReviewCount() {
        long count = reviewService.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> reviewExists(@PathVariable Long id) {
        boolean exists = reviewService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}
