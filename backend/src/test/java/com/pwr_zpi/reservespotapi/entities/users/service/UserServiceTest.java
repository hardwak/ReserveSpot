package com.pwr_zpi.reservespotapi.entities.users.service;

import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.RestaurantDto;
import com.pwr_zpi.reservespotapi.entities.users.Role;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import com.pwr_zpi.reservespotapi.entities.users.dto.CreateUserDto;
import com.pwr_zpi.reservespotapi.entities.users.dto.UpdateProfileDto;
import com.pwr_zpi.reservespotapi.entities.users.dto.UpdateUserDto;
import com.pwr_zpi.reservespotapi.entities.users.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private User testUser;
    private User restaurantOwner;
    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        restaurantRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .email("test@example.com")
                .passwordHash("password")
                .role(Role.CLIENT)
                .name("Test User")
                .phoneNumber("+1234567890")
                .build();
        testUser = userRepository.save(testUser);

        restaurantOwner = User.builder()
                .email("owner@restaurant.com")
                .passwordHash("password")
                .role(Role.RESTAURANT)
                .name("Restaurant Owner")
                .build();
        restaurantOwner = userRepository.save(restaurantOwner);

        Map<String, String> openingHours = new HashMap<>();
        openingHours.put("monday", "10:00-22:00");
        testRestaurant = Restaurant.builder()
                .owner(restaurantOwner)
                .name("Test Restaurant")
                .address("123 Test St")
                .city("Test City")
                .openingHours(openingHours)
                .build();
        testRestaurant = restaurantRepository.save(testRestaurant);
    }

    @Test
    void testCreateUser_Success() {
        // Given
        CreateUserDto createDto = CreateUserDto.builder()
                .email("newuser@example.com")
                .passwordHash("password123")
                .role(Role.CLIENT)
                .name("New User")
                .phoneNumber("+1234567891")
                .build();

        // When
        UserDto result = userService.createUser(createDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("newuser@example.com", result.getEmail());
        assertEquals("New User", result.getName());
        assertEquals(Role.CLIENT, result.getRole());
    }

    @Test
    void testCreateUser_DuplicateEmail_ThrowsException() {
        // Given
        CreateUserDto createDto = CreateUserDto.builder()
                .email("test@example.com") // Already exists
                .passwordHash("password123")
                .role(Role.CLIENT)
                .name("Duplicate User")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(createDto));
    }

    @Test
    void testGetUserById_Success() {
        // When
        Optional<UserDto> result = userService.getUserById(testUser.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void testGetUserById_NotFound() {
        // When
        Optional<UserDto> result = userService.getUserById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testGetUserByEmail_Success() {
        // When
        Optional<UserDto> result = userService.getUserByEmail("test@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void testUpdateUser_Success() {
        // Given
        UpdateUserDto updateDto = UpdateUserDto.builder()
                .name("Updated Name")
                .phoneNumber("+9876543210")
                .build();

        // When
        Optional<UserDto> result = userService.updateUser(testUser.getId(), updateDto);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Updated Name", result.get().getName());
        assertEquals("+9876543210", result.get().getPhoneNumber());
    }

    @Test
    void testUpdateProfile_Success() {
        // Given
        UpdateProfileDto updateDto = UpdateProfileDto.builder()
                .name("Updated Profile Name")
                .email("updated@example.com")
                .phoneNumber("+1111111111")
                .build();

        // When
        UserDto result = userService.updateProfile(testUser.getId(), updateDto);

        // Then
        assertNotNull(result);
        assertEquals("Updated Profile Name", result.getName());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("+1111111111", result.getPhoneNumber());
    }

    @Test
    void testUpdateProfile_BlankName_ThrowsException() {
        // Given
        UpdateProfileDto updateDto = UpdateProfileDto.builder()
                .name("   ") // Blank name
                .email("test@example.com")
                .build();

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.updateProfile(testUser.getId(), updateDto));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testDeleteUser_Success() {
        // When
        boolean deleted = userService.deleteUser(testUser.getId());

        // Then
        assertTrue(deleted);
        assertFalse(userRepository.existsById(testUser.getId()));
    }

    @Test
    void testAddFavoriteRestaurant_Success() {
        // When
        RestaurantDto result = userService.addFavoriteRestaurant(testUser.getId(), testRestaurant.getId());

        // Then
        assertNotNull(result);
        assertEquals(testRestaurant.getId(), result.getId());

        // Verify it was added
        List<RestaurantDto> favorites = userService.getFavoriteRestaurants(testUser.getId());
        assertEquals(1, favorites.size());
        assertEquals(testRestaurant.getId(), favorites.get(0).getId());
    }

    @Test
    void testAddFavoriteRestaurant_Duplicate_ThrowsException() {
        // Given
        userService.addFavoriteRestaurant(testUser.getId(), testRestaurant.getId());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.addFavoriteRestaurant(testUser.getId(), testRestaurant.getId()));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void testRemoveFavoriteRestaurant_Success() {
        // Given
        userService.addFavoriteRestaurant(testUser.getId(), testRestaurant.getId());

        // When
        userService.removeFavoriteRestaurant(testUser.getId(), testRestaurant.getId());

        // Then
        List<RestaurantDto> favorites = userService.getFavoriteRestaurants(testUser.getId());
        assertTrue(favorites.isEmpty());
    }

    @Test
    void testRemoveFavoriteRestaurant_NotInFavorites_ThrowsException() {
        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.removeFavoriteRestaurant(testUser.getId(), testRestaurant.getId()));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testGetFavoriteRestaurants_Empty() {
        // When
        List<RestaurantDto> favorites = userService.getFavoriteRestaurants(testUser.getId());

        // Then
        assertNotNull(favorites);
        assertTrue(favorites.isEmpty());
    }

    @Test
    void testGetFavoriteRestaurants_WithFavorites() {
        // Given
        userService.addFavoriteRestaurant(testUser.getId(), testRestaurant.getId());

        // When
        List<RestaurantDto> favorites = userService.getFavoriteRestaurants(testUser.getId());

        // Then
        assertNotNull(favorites);
        assertEquals(1, favorites.size());
        assertEquals(testRestaurant.getId(), favorites.get(0).getId());
    }

    @Test
    void testIsFavoriteRestaurant_True() {
        // Given
        userService.addFavoriteRestaurant(testUser.getId(), testRestaurant.getId());

        // When
        boolean isFavorite = userService.isFavoriteRestaurant(testUser.getId(), testRestaurant.getId());

        // Then
        assertTrue(isFavorite);
    }

    @Test
    void testIsFavoriteRestaurant_False() {
        // When
        boolean isFavorite = userService.isFavoriteRestaurant(testUser.getId(), testRestaurant.getId());

        // Then
        assertFalse(isFavorite);
    }

    @Test
    void testGetUsersByRole() {
        // Given
        User client2 = User.builder()
                .email("client2@example.com")
                .passwordHash("password")
                .role(Role.CLIENT)
                .name("Client 2")
                .build();
        userRepository.save(client2);

        // When
        List<UserDto> clients = userService.getUsersByRole(Role.CLIENT);

        // Then
        assertNotNull(clients);
        assertTrue(clients.size() >= 2);
        assertTrue(clients.stream().allMatch(u -> u.getRole() == Role.CLIENT));
    }
}

