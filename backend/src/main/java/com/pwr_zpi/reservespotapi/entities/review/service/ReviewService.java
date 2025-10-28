package com.pwr_zpi.reservespotapi.entities.review.service;

import com.pwr_zpi.reservespotapi.entities.review.dto.CreateReviewDto;
import com.pwr_zpi.reservespotapi.entities.review.dto.ReviewDto;
import com.pwr_zpi.reservespotapi.entities.review.dto.UpdateReviewDto;
import com.pwr_zpi.reservespotapi.entities.review.Review;
import com.pwr_zpi.reservespotapi.entities.review.ReviewRepository;
import com.pwr_zpi.reservespotapi.entities.review.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

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

    public ReviewDto createReview(CreateReviewDto createDto) {
        Review review = reviewMapper.toEntity(createDto);
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

    public boolean deleteReview(Long id) {
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean existsById(Long id) {
        return reviewRepository.existsById(id);
    }

    public long count() {
        return reviewRepository.count();
    }
}
