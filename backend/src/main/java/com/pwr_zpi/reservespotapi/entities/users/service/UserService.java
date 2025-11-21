package com.pwr_zpi.reservespotapi.entities.users.service;

import com.pwr_zpi.reservespotapi.entities.reservation.Reservation;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant.service.RestaurantService;
import com.pwr_zpi.reservespotapi.entities.review.Review;
import com.pwr_zpi.reservespotapi.entities.review.ReviewRepository;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import com.pwr_zpi.reservespotapi.entities.users.dto.CreateUserDto;
import com.pwr_zpi.reservespotapi.entities.users.dto.UpdateProfileDto;
import com.pwr_zpi.reservespotapi.entities.users.dto.UpdateUserDto;
import com.pwr_zpi.reservespotapi.entities.users.dto.UserDto;
import com.pwr_zpi.reservespotapi.entities.users.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final RestaurantService restaurantService;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto);
    }

    public Optional<UserDto> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toDto);
    }

    public List<UserDto> getUsersByRole(com.pwr_zpi.reservespotapi.entities.users.Role role) {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == role)
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto createUser(CreateUserDto createDto) {
        // Check if email already exists
        if (userRepository.findByEmail(createDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with email " + createDto.getEmail() + " already exists");
        }

        User user = userMapper.toEntity(createDto);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    public Optional<UserDto> updateUser(Long id, UpdateUserDto updateDto) {
        return userRepository.findById(id)
                .map(user -> {
                    // Check if email is being changed and if it already exists
                    if (updateDto.getEmail() != null && 
                        !updateDto.getEmail().equals(user.getEmail()) &&
                        userRepository.findByEmail(updateDto.getEmail()).isPresent()) {
                        throw new IllegalArgumentException("User with email " + updateDto.getEmail() + " already exists");
                    }

                    userMapper.updateEntity(updateDto, user);
                    User savedUser = userRepository.save(user);
                    return userMapper.toDto(savedUser);
                });
    }

    public boolean deleteUser(Long id) {
        return userRepository.findById(id)
            .map(user -> {
                // Delete all reservations for this user
                if (user.getReservations() != null && !user.getReservations().isEmpty()) {
                    List<Reservation> reservations = user.getReservations().stream().toList();
                    reservationRepository.deleteAll(reservations);
                }
                
                // Delete all reviews for this user
                if (user.getReviews() != null && !user.getReviews().isEmpty()) {
                    List<Review> reviews = user.getReviews().stream().toList();
                    reviewRepository.deleteAll(reviews);
                }
                
                // Delete all restaurants owned by this user
                // Use RestaurantService.deleteRestaurant to ensure proper cascade deletion
                if (user.getRestaurants() != null && !user.getRestaurants().isEmpty()) {
                    List<Restaurant> restaurants = user.getRestaurants().stream().toList();
                    for (Restaurant restaurant : restaurants) {
                        restaurantService.deleteRestaurant(restaurant.getId());
                    }
                }
                
                // The picture will be deleted automatically due to cascade
                // Now delete the user
                userRepository.delete(user);
                return true;
            })
            .orElse(false);
    }

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public long count() {
        return userRepository.count();
    }

    public UserDto updateProfile(Long userId, UpdateProfileDto updateDto) {
        return userRepository.findById(userId)
                .map(user -> {
                    String trimmedName = updateDto.getName().trim();
                    if (trimmedName.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must not be blank");
                    }

                    String normalizedEmail = updateDto.getEmail().trim().toLowerCase();
                    if (!normalizedEmail.equalsIgnoreCase(user.getEmail())
                            && userRepository.findByEmail(normalizedEmail).isPresent()) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already taken");
                    }

                    user.setName(trimmedName);
                    user.setEmail(normalizedEmail);
                    user.setPhoneNumber(updateDto.getPhoneNumber());

                    User savedUser = userRepository.save(user);
                    return userMapper.toDto(savedUser);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}

