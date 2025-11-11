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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Reservation reservation = null;
        if (createDto.getReservationId() != null) {
            reservation = reservationRepository.findById(createDto.getReservationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));

            if (!reservation.getUser().getId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reservation does not belong to current user");
            }
        }

        Restaurant restaurant = resolveRestaurant(createDto, reservation);

        if (reviewRepository.existsByUserIdAndRestaurantId(userId, restaurant.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review already exists for this restaurant");
        }

        validateReservationEligibility(createDto, reservation, userId, restaurant.getId());

        Review review = reviewMapper.toEntity(createDto, user, restaurant);
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toDto(savedReview);
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

    private Restaurant resolveRestaurant(CreateReviewDto createDto, Reservation reservationFromRequest) {
        if (reservationFromRequest != null) {
            Restaurant restaurant = reservationFromRequest.getTable().getRestaurant();

            if (createDto.getRestaurantId() != null
                    && !restaurant.getId().equals(createDto.getRestaurantId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation does not match restaurant");
            }

            return restaurant;
        }

        return restaurantRepository.findById(createDto.getRestaurantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));
    }

    private void validateReservationEligibility(CreateReviewDto createDto,
                                                Reservation reservation,
                                                Long userId,
                                                Long restaurantId) {
        if (reservation != null) {
            if (reservation.getStatus() == ReservationStatus.CANCELLED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot review a cancelled reservation");
            }
            if (!reservation.getTable().getRestaurant().getId().equals(restaurantId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation does not match restaurant");
            }
            if (!reservation.getReservationDatetime().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation is not completed yet");
            }
            return;
        }

        boolean hasPastReservation = reservationRepository.hasPastReservationForRestaurant(
                userId,
                restaurantId,
                LocalDateTime.now(),
                ReservationStatus.CANCELLED
        );

        if (!hasPastReservation) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no completed reservations for this restaurant");
        }
    }
}
