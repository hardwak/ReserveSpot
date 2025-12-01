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
import com.pwr_zpi.reservespotapi.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
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
    private final StorageService storageService;

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

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/jpg",
            "image/webp"
    );


    public PictureDto uploadPicture(MultipartFile file, String description) {
        validateFile(file);

        String fileUrl = storageService.uploadFile(file);

        Picture picture = Picture.builder()
                .url(fileUrl)
                .description(description)
                .uploadedAt(LocalDateTime.now())
                .build();

        Picture savedPicture = pictureRepository.save(picture);

        return pictureMapper.toDto(savedPicture);
    }

    public PictureDto uploadPictureToRestaurant(Long restaurantId, MultipartFile file, String description, Long userId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));

        if (!restaurant.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this restaurant");
        }

        validateFile(file);
        String fileUrl = storageService.uploadFile(file);

        Picture picture = Picture.builder()
                .url(fileUrl)
                .description(description)
                .uploadedAt(LocalDateTime.now())
                .build();

        Picture savedPicture = pictureRepository.save(picture);

        restaurant.getPictures().add(savedPicture);
        restaurantRepository.save(restaurant);

        return pictureMapper.toDto(savedPicture);
    }

    public void deletePictureFromRestaurant(Long restaurantId, Long pictureId, Long userId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));

        if (!restaurant.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this restaurant");
        }

        Picture picture = pictureRepository.findById(pictureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Picture not found"));

        if (!restaurant.getPictures().contains(picture)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This picture does not belong to the specified restaurant");
        }

        restaurant.getPictures().remove(picture);
        restaurantRepository.save(restaurant);

        if (isOrphan(picture)) {
            storageService.deleteFile(picture.getUrl());
            pictureRepository.delete(picture);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Invalid file type. Only images (JPG, PNG, WebP) are allowed"
            );
        }
    }

    private boolean isOrphan(Picture picture) {
        boolean usedByRestaurants = !picture.getRestaurants().isEmpty();
        boolean usedByReviews = !picture.getReviews().isEmpty();
        boolean usedByUser = picture.getUser() != null;

        return !usedByRestaurants && !usedByReviews && !usedByUser;
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
                
                storageService.deleteFile(picture.getUrl());
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
