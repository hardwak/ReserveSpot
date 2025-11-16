package com.pwr_zpi.reservespotapi.entities.review.controller;

import com.pwr_zpi.reservespotapi.entities.review.dto.CreateReviewDto;
import com.pwr_zpi.reservespotapi.entities.review.dto.ReviewDto;
import com.pwr_zpi.reservespotapi.entities.review.dto.ReviewEligibilityResponse;
import com.pwr_zpi.reservespotapi.entities.review.dto.UpdateReviewDto;
import com.pwr_zpi.reservespotapi.entities.review.service.ReviewService;
import com.pwr_zpi.reservespotapi.entities.users.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;
    private final CurrentUserService currentUserService;

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

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByRestaurant(@PathVariable Long restaurantId) {
        List<ReviewDto> reviews = reviewService.getReviewsByRestaurantId(restaurantId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReviewDto>> getReviewsByUser(@PathVariable Long userId) {
        List<ReviewDto> reviews = reviewService.getReviewsByUserId(userId);
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

    @GetMapping("/can-create")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReviewEligibilityResponse> canCreateReview(
            HttpServletRequest request,
            @RequestParam Long restaurantId,
            @RequestParam(required = false) Long reservationId) {
        Long userId = currentUserService.requireCurrentUserId(request);
        ReviewEligibilityResponse response = reviewService.canCreateReview(userId, restaurantId, reservationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReviewDto> createReview(HttpServletRequest request,
                                                  @Valid @RequestBody CreateReviewDto createDto) {
        Long userId = currentUserService.requireCurrentUserId(request);
        ReviewDto createdReview = reviewService.createReview(createDto, userId);
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
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> deleteReview(HttpServletRequest request, @PathVariable Long id) {
        Long userId = currentUserService.requireCurrentUserId(request);
        reviewService.deleteReview(id, userId);
        return ResponseEntity.noContent().build();
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

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<ReviewDto>> getMyReviews(HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        List<ReviewDto> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(reviews);
    }
}
