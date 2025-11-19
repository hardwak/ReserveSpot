package com.pwr_zpi.reservespotapi.entities.picture.service;

import com.pwr_zpi.reservespotapi.entities.picture.dto.CreatePictureDto;
import com.pwr_zpi.reservespotapi.entities.picture.dto.PictureDto;
import com.pwr_zpi.reservespotapi.entities.picture.dto.UpdatePictureDto;
import com.pwr_zpi.reservespotapi.entities.picture.Picture;
import com.pwr_zpi.reservespotapi.entities.picture.PictureRepository;
import com.pwr_zpi.reservespotapi.entities.picture.mapper.PictureMapper;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.review.Review;
import com.pwr_zpi.reservespotapi.entities.review.ReviewRepository;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PictureService {

    private final PictureRepository pictureRepository;
    private final PictureMapper pictureMapper;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public List<PictureDto> getAllPictures() {
        return pictureRepository.findAll()
                .stream()
                .map(pictureMapper::toDto)
                .toList();
    }

    public Optional<PictureDto> getPictureById(Long id) {
        return pictureRepository.findById(id)
                .map(pictureMapper::toDto);
    }

    public List<PictureDto> getPicturesByUrl(String url) {
        return pictureRepository.findByUrlContainingIgnoreCase(url)
                .stream()
                .map(pictureMapper::toDto)
                .toList();
    }

    public List<PictureDto> getPicturesByDescription(String description) {
        return pictureRepository.findByDescriptionContainingIgnoreCase(description)
                .stream()
                .map(pictureMapper::toDto)
                .toList();
    }

    public PictureDto createPicture(CreatePictureDto createDto) {
        Picture picture = pictureMapper.toEntity(createDto);
        Picture savedPicture = pictureRepository.save(picture);
        return pictureMapper.toDto(savedPicture);
    }

    public Optional<PictureDto> updatePicture(Long id, UpdatePictureDto updateDto) {
        return pictureRepository.findById(id)
                .map(picture -> {
                    pictureMapper.updateEntity(updateDto, picture);
                    Picture savedPicture = pictureRepository.save(picture);
                    return pictureMapper.toDto(savedPicture);
                });
    }

    public boolean deletePicture(Long id) {
        return pictureRepository.findById(id)
            .map(picture -> {
                // Remove this picture from all restaurants that have it
                if (picture.getRestaurants() != null && !picture.getRestaurants().isEmpty()) {
                    // Create a copy of the set to avoid ConcurrentModificationException
                    List<Restaurant> restaurantsWithPicture = picture.getRestaurants().stream().toList();
                    for (Restaurant restaurant : restaurantsWithPicture) {
                        if (restaurant.getPictures() != null) {
                            restaurant.getPictures().remove(picture);
                            restaurantRepository.save(restaurant); // Save to update the join table
                        }
                    }
                }
                
                // Remove this picture from all reviews that have it
                if (picture.getReviews() != null && !picture.getReviews().isEmpty()) {
                    // Create a copy of the set to avoid ConcurrentModificationException
                    List<Review> reviewsWithPicture = picture.getReviews().stream().toList();
                    for (Review review : reviewsWithPicture) {
                        if (review.getPictures() != null) {
                            review.getPictures().remove(picture);
                            reviewRepository.save(review); // Save to update the join table
                        }
                    }
                }
                
                // Find and update the user that has this picture (if any)
                // Since User has @OneToOne with cascade, we need to set it to null
                Optional<User> userWithPicture = userRepository.findAll().stream()
                    .filter(user -> user.getPicture() != null && user.getPicture().getId().equals(id))
                    .findFirst();
                if (userWithPicture.isPresent()) {
                    User user = userWithPicture.get();
                    user.setPicture(null);
                    userRepository.save(user);
                }
                
                // Now delete the picture
                pictureRepository.delete(picture);
            return true;
            })
            .orElse(false);
    }

    public boolean existsById(Long id) {
        return pictureRepository.existsById(id);
    }

    public long count() {
        return pictureRepository.count();
    }
}
