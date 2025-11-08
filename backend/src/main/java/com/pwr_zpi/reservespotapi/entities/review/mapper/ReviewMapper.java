package com.pwr_zpi.reservespotapi.entities.review.mapper;

import com.pwr_zpi.reservespotapi.entities.review.dto.CreateReviewDto;
import com.pwr_zpi.reservespotapi.entities.review.dto.ReviewDto;
import com.pwr_zpi.reservespotapi.entities.review.dto.UpdateReviewDto;
import com.pwr_zpi.reservespotapi.entities.review.Review;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.users.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ReviewMapper {

    public ReviewDto toDto(Review review) {
        if (review == null) {
            return null;
        }

        return ReviewDto.builder()
                .id(review.getId())
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .restaurantId(review.getRestaurant() != null ? review.getRestaurant().getId() : null)
                .phoneNumber(review.getPhoneNumber())
                .rating(review.getRating())
                .comment(review.getComment())
                .pic(review.getPic())
                .createdAt(review.getCreatedAt())
                .pictureIds(review.getPictures() != null ? 
                    review.getPictures().stream().map(picture -> picture.getId()).collect(Collectors.toSet()) : null)
                .build();
    }

    public Review toEntity(CreateReviewDto dto) {
        if (dto == null) {
            return null;
        }

        Review review = Review.builder()
                .phoneNumber(dto.getPhoneNumber())
                .rating(dto.getRating())
                .comment(dto.getComment())
                .pic(dto.getPic())
                .createdAt(java.time.LocalDateTime.now())
                .build();

        // Set user if provided
        if (dto.getUserId() != null) {
            User user = new User();
            user.setId(dto.getUserId());
            review.setUser(user);
        }

        // Set restaurant if provided
        if (dto.getRestaurantId() != null) {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(dto.getRestaurantId());
            review.setRestaurant(restaurant);
        }

        return review;
    }

    public void updateEntity(UpdateReviewDto dto, Review review) {
        if (dto == null || review == null) {
            return;
        }

        if (dto.getPhoneNumber() != null) {
            review.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getRating() != null) {
            review.setRating(dto.getRating());
        }
        if (dto.getComment() != null) {
            review.setComment(dto.getComment());
        }
        if (dto.getPic() != null) {
            review.setPic(dto.getPic());
        }
    }
}
