package com.pwr_zpi.reservespotapi.entities.restaurant.service;

import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.CreateRestaurantDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.RestaurantDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.RestaurantSearchDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.UpdateRestaurantDto;
import com.pwr_zpi.reservespotapi.entities.tag.Tag;
import com.pwr_zpi.reservespotapi.entities.tag.TagRepository;
import com.pwr_zpi.reservespotapi.entities.users.Role;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RestaurantServiceTest {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    private User restaurantOwner;
    private Tag italianTag;
    private Tag pizzaTag;

    @BeforeEach
    void setUp() {
        restaurantRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();

        restaurantOwner = User.builder()
                .email("owner@restaurant.com")
                .passwordHash("password")
                .role(Role.RESTAURANT)
                .name("Restaurant Owner")
                .build();
        restaurantOwner = userRepository.save(restaurantOwner);

        italianTag = Tag.builder().name("Italian").build();
        italianTag = tagRepository.save(italianTag);

        pizzaTag = Tag.builder().name("Pizza").build();
        pizzaTag = tagRepository.save(pizzaTag);
    }

    @Test
    void testCreateRestaurant_Success() {
        // Given
        // Note: CreateRestaurantDto expects openingHours as JSON string, but mapper will handle it
       
        Map<String, String> openingHours = new HashMap<>();
        openingHours.put("monday", "10:00-22:00");

        CreateRestaurantDto createDto = CreateRestaurantDto.builder()
                .ownerId(restaurantOwner.getId())
                .name("Test Restaurant")
                .address("123 Test St")
                .city("Test City")
                .description("A test restaurant")
                .openingHours("{\"monday\":\"10:00-22:00\"}") // JSON string format
                .tagIds(Set.of(italianTag.getId(), pizzaTag.getId()))
                .build();

        // When
        RestaurantDto result = restaurantService.createRestaurant(createDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test Restaurant", result.getName());
        assertEquals("Test City", result.getCity());
        assertEquals(restaurantOwner.getId(), result.getOwnerId());
        assertNotNull(result.getTagIds());
        assertTrue(result.getTagIds().contains(italianTag.getId()));
        assertTrue(result.getTagIds().contains(pizzaTag.getId()));
    }

    @Test
    void testGetRestaurantById_Success() {
        // Given
        Map<String, String> openingHours = new HashMap<>();
        openingHours.put("monday", "10:00-22:00");

        Restaurant restaurant = Restaurant.builder()
                .owner(restaurantOwner)
                .name("Test Restaurant")
                .address("123 Test St")
                .city("Test City")
                .description("A test restaurant")
                .openingHours(openingHours)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        // When
        Optional<RestaurantDto> result = restaurantService.getRestaurantById(restaurant.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(restaurant.getId(), result.get().getId());
        assertEquals("Test Restaurant", result.get().getName());
    }

    @Test
    void testGetRestaurantById_NotFound() {
        // When
        Optional<RestaurantDto> result = restaurantService.getRestaurantById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testGetAllRestaurants() {
        // Given
        Map<String, String> openingHours = new HashMap<>();
        openingHours.put("monday", "10:00-22:00");

        Restaurant restaurant1 = Restaurant.builder()
                .owner(restaurantOwner)
                .name("Restaurant 1")
                .city("City 1")
                .openingHours(openingHours)
                .build();
        restaurantRepository.save(restaurant1);

        Restaurant restaurant2 = Restaurant.builder()
                .owner(restaurantOwner)
                .name("Restaurant 2")
                .city("City 2")
                .openingHours(openingHours)
                .build();
        restaurantRepository.save(restaurant2);

        // When
        List<RestaurantDto> result = restaurantService.getAllRestaurants();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetRestaurantsByCity() {
        // Given
        Map<String, String> openingHours = new HashMap<>();
        openingHours.put("monday", "10:00-22:00");

        Restaurant restaurant1 = Restaurant.builder()
                .owner(restaurantOwner)
                .name("Restaurant 1")
                .city("New York")
                .openingHours(openingHours)
                .build();
        restaurantRepository.save(restaurant1);

        Restaurant restaurant2 = Restaurant.builder()
                .owner(restaurantOwner)
                .name("Restaurant 2")
                .city("Los Angeles")
                .openingHours(openingHours)
                .build();
        restaurantRepository.save(restaurant2);

        // When
        List<RestaurantDto> result = restaurantService.getRestaurantsByCity("New York");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("New York", result.get(0).getCity());
    }

    @Test
    void testGetRestaurantsByOwnerId() {
        // Given
        User anotherOwner = User.builder()
                .email("another@owner.com")
                .passwordHash("password")
                .role(Role.RESTAURANT)
                .build();
        anotherOwner = userRepository.save(anotherOwner);

        Map<String, String> openingHours = new HashMap<>();
        openingHours.put("monday", "10:00-22:00");

        Restaurant restaurant1 = Restaurant.builder()
                .owner(restaurantOwner)
                .name("Owner 1 Restaurant")
                .city("City 1")
                .openingHours(openingHours)
                .build();
        restaurantRepository.save(restaurant1);

        Restaurant restaurant2 = Restaurant.builder()
                .owner(anotherOwner)
                .name("Owner 2 Restaurant")
                .city("City 2")
                .openingHours(openingHours)
                .build();
        restaurantRepository.save(restaurant2);

        // When
        List<RestaurantDto> result = restaurantService.getRestaurantsByOwnerId(restaurantOwner.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(restaurantOwner.getId(), result.get(0).getOwnerId());
    }

    @Test
    void testUpdateRestaurant_Success() {
        // Given
        Map<String, String> openingHours = new HashMap<>();
        openingHours.put("monday", "10:00-22:00");

        Restaurant restaurant = Restaurant.builder()
                .owner(restaurantOwner)
                .name("Original Name")
                .address("Original Address")
                .city("Original City")
                .openingHours(openingHours)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        UpdateRestaurantDto updateDto = UpdateRestaurantDto.builder()
                .name("Updated Name")
                .address("Updated Address")
                .tagIds(Set.of(italianTag.getId()))
                .build();

        // When
        Optional<RestaurantDto> result = restaurantService.updateRestaurant(restaurant.getId(), updateDto);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Updated Name", result.get().getName());
        assertEquals("Updated Address", result.get().getAddress());
        assertTrue(result.get().getTagIds().contains(italianTag.getId()));
    }

    @Test
    void testDeleteRestaurant_Success() {
        // Given
        Map<String, String> openingHours = new HashMap<>();
        openingHours.put("monday", "10:00-22:00");

        Restaurant restaurant = Restaurant.builder()
                .owner(restaurantOwner)
                .name("To Delete")
                .city("City")
                .openingHours(openingHours)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        // When
        boolean deleted = restaurantService.deleteRestaurant(restaurant.getId());

        // Then
        assertTrue(deleted);
        assertFalse(restaurantRepository.existsById(restaurant.getId()));
    }

    @Test
    void testSearchRestaurants_ByName() {
        // Given
        Map<String, String> openingHours = new HashMap<>();
        openingHours.put("monday", "10:00-22:00");

        Restaurant restaurant1 = Restaurant.builder()
                .owner(restaurantOwner)
                .name("Italian Bistro")
                .description("Authentic Italian cuisine")
                .city("New York")
                .openingHours(openingHours)
                .build();
        restaurantRepository.save(restaurant1);

        Restaurant restaurant2 = Restaurant.builder()
                .owner(restaurantOwner)
                .name("Sushi Bar")
                .description("Fresh sushi")
                .city("Los Angeles")
                .openingHours(openingHours)
                .build();
        restaurantRepository.save(restaurant2);

        RestaurantSearchDto searchDto = RestaurantSearchDto.builder()
                .query("Italian")
                .build();

        // When
        List<RestaurantDto> result = restaurantService.searchRestaurants(searchDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getName().contains("Italian"));
    }

    @Test
    void testSearchRestaurants_ByCity() {
        // Given
        Map<String, String> openingHours = new HashMap<>();
        openingHours.put("monday", "10:00-22:00");

        Restaurant restaurant1 = Restaurant.builder()
                .owner(restaurantOwner)
                .name("Restaurant 1")
                .city("New York")
                .openingHours(openingHours)
                .build();
        restaurantRepository.save(restaurant1);

        Restaurant restaurant2 = Restaurant.builder()
                .owner(restaurantOwner)
                .name("Restaurant 2")
                .city("Los Angeles")
                .openingHours(openingHours)
                .build();
        restaurantRepository.save(restaurant2);

        RestaurantSearchDto searchDto = RestaurantSearchDto.builder()
                .city("New York")
                .build();

        // When
        List<RestaurantDto> result = restaurantService.searchRestaurants(searchDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("New York", result.get(0).getCity());
    }

    @Test
    void testSearchRestaurants_ByRating() {
        // Given
        Map<String, String> openingHours = new HashMap<>();
        openingHours.put("monday", "10:00-22:00");

        Restaurant restaurant1 = Restaurant.builder()
                .owner(restaurantOwner)
                .name("High Rated")
                .city("City")
                .averageRating(4.8)
                .openingHours(openingHours)
                .build();
        restaurantRepository.save(restaurant1);

        Restaurant restaurant2 = Restaurant.builder()
                .owner(restaurantOwner)
                .name("Low Rated")
                .city("City")
                .averageRating(3.2)
                .openingHours(openingHours)
                .build();
        restaurantRepository.save(restaurant2);

        RestaurantSearchDto searchDto = RestaurantSearchDto.builder()
                .minRating(4.0)
                .build();

        // When
        List<RestaurantDto> result = restaurantService.searchRestaurants(searchDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getAverageRating() >= 4.0);
    }
}

