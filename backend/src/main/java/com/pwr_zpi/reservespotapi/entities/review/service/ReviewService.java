package com.pwr_zpi.reservespotapi.entities.review.service;

import com.pwr_zpi.reservespotapi.entities.reservation.Reservation;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationRepository;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationStatus;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.review.Review;
import com.pwr_zpi.reservespotapi.entities.review.ReviewRepository;
import com.pwr_zpi.reservespotapi.entities.review.dto.CreateReviewDto;
import com.pwr_zpi.reservespotapi.entities.review.dto.ReviewDto;
import com.pwr_zpi.reservespotapi.entities.review.dto.ReviewEligibilityResponse;
import com.pwr_zpi.reservespotapi.entities.review.dto.UpdateReviewDto;
import com.pwr_zpi.reservespotapi.entities.review.mapper.ReviewMapper;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReservationRepository reservationRepository;

    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    public Optional<ReviewDto> getReviewById(Long id) {
        return reviewRepository.findById(id)
                .map(reviewMapper::toDto);
    }

    public List<ReviewDto> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId)
                .stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    public List<ReviewDto> getReviewsByRestaurantId(Long restaurantId) {
        return reviewRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    public List<ReviewDto> getReviewsByRating(Integer rating) {
        return reviewRepository.findByRating(rating)
                .stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    public List<ReviewDto> getReviewsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return reviewRepository.findByCreatedAtBetween(startDate, endDate)
                .stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    public ReviewDto createReview(CreateReviewDto createDto, Long userId) {
        ReviewEligibilityResult eligibility = evaluateEligibility(userId, createDto.getRestaurantId(), createDto.getReservationId());

        if (!eligibility.canReview()) {
            throw new ResponseStatusException(eligibility.failureStatus(), eligibility.failureMessage());
        }

        Review review = reviewMapper.toEntity(createDto, eligibility.user(), eligibility.restaurant());
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toDto(savedReview);
    }

    public ReviewEligibilityResponse canCreateReview(Long userId, Long restaurantId, Long reservationId) {
        ReviewEligibilityResult eligibility = evaluateEligibility(userId, restaurantId, reservationId);
        if (eligibility.canReview()) {
            return new ReviewEligibilityResponse(true, "Eligible to create review");
        }
        return new ReviewEligibilityResponse(false, eligibility.failureMessage());
    }

    public Optional<ReviewDto> updateReview(Long id, UpdateReviewDto updateDto) {
        return reviewRepository.findById(id)
                .map(review -> {
                    reviewMapper.updateEntity(updateDto, review);
                    Review savedReview = reviewRepository.save(review);
                    return reviewMapper.toDto(savedReview);
                });
    }

    public void deleteReview(Long id, Long userId) {
        Review review = reviewRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        reviewRepository.delete(review);
    }

    public boolean existsById(Long id) {
        return reviewRepository.existsById(id);
    }

    public long count() {
        return reviewRepository.count();
    }

    private ReviewEligibilityResult evaluateEligibility(Long userId, Long restaurantId, Long reservationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));

        if (reviewRepository.existsByUserIdAndRestaurantId(userId, restaurantId)) {
            return ReviewEligibilityResult.failure(user, restaurant, null, HttpStatus.CONFLICT,
                    "Review already exists for this restaurant");
        }

        Reservation reservation = null;
        if (reservationId != null) {
            reservation = reservationRepository.findById(reservationId)
                    .orElse(null);

            if (reservation == null) {
                return ReviewEligibilityResult.failure(user, restaurant, null, HttpStatus.NOT_FOUND, "Reservation not found");
            }

            if (!reservation.getUser().getId().equals(userId)) {
                return ReviewEligibilityResult.failure(user, restaurant, reservation, HttpStatus.FORBIDDEN,
                        "Reservation does not belong to current user");
            }

            if (!reservation.getTable().getRestaurant().getId().equals(restaurantId)) {
                return ReviewEligibilityResult.failure(user, restaurant, reservation, HttpStatus.BAD_REQUEST,
                        "Reservation does not match restaurant");
            }

            if (reservation.getStatus() == ReservationStatus.CANCELLED) {
                return ReviewEligibilityResult.failure(user, restaurant, reservation, HttpStatus.BAD_REQUEST,
                        "Cannot review a cancelled reservation");
            }

            if (!reservation.getReservationDatetime().isBefore(LocalDateTime.now())) {
                return ReviewEligibilityResult.failure(user, restaurant, reservation, HttpStatus.BAD_REQUEST,
                        "Reservation is not completed yet");
            }
        } else {
            boolean hasPastReservation = reservationRepository.hasPastReservationForRestaurant(
                    userId,
                    restaurantId,
                    LocalDateTime.now(),
                    ReservationStatus.CANCELLED
            );

            if (!hasPastReservation) {
                return ReviewEligibilityResult.failure(user, restaurant, null, HttpStatus.FORBIDDEN,
                        "User has no completed reservations for this restaurant");
            }
        }

        return ReviewEligibilityResult.success(user, restaurant, reservation);
    }

    private record ReviewEligibilityResult(
            User user,
            Restaurant restaurant,
            Reservation reservation,
            boolean canReview,
            HttpStatus failureStatus,
            String failureMessage
    ) {
        static ReviewEligibilityResult success(User user, Restaurant restaurant, Reservation reservation) {
            return new ReviewEligibilityResult(user, restaurant, reservation, true, null, null);
        }

        static ReviewEligibilityResult failure(User user, Restaurant restaurant, Reservation reservation,
                                               HttpStatus status, String message) {
            return new ReviewEligibilityResult(user, restaurant, reservation, false, status, message);
        }
    }
}
