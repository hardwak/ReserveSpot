package com.pwr_zpi.reservespotapi.controller.restaurant;

import com.pwr_zpi.reservespotapi.dto.restaurant.CreateRestaurantDto;
import com.pwr_zpi.reservespotapi.dto.restaurant.RestaurantDto;
import com.pwr_zpi.reservespotapi.dto.restaurant.UpdateRestaurantDto;
import com.pwr_zpi.reservespotapi.service.restaurant.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Restaurant Management", description = "APIs for managing restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    @Operation(summary = "Get all restaurants", description = "Retrieve a list of all restaurants")
    public ResponseEntity<List<RestaurantDto>> getAllRestaurants() {
        List<RestaurantDto> restaurants = restaurantService.getAllRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto> getRestaurantById(@PathVariable Long id) {
        Optional<RestaurantDto> restaurant = restaurantService.getRestaurantById(id);
        return restaurant.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<RestaurantDto>> getRestaurantsByOwner(@PathVariable Long ownerId) {
        List<RestaurantDto> restaurants = restaurantService.getRestaurantsByOwnerId(ownerId);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<RestaurantDto>> getRestaurantsByCity(@PathVariable String city) {
        List<RestaurantDto> restaurants = restaurantService.getRestaurantsByCity(city);
        return ResponseEntity.ok(restaurants);
    }

    @PostMapping
    public ResponseEntity<RestaurantDto> createRestaurant(@Valid @RequestBody CreateRestaurantDto createDto) {
        RestaurantDto createdRestaurant = restaurantService.createRestaurant(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRestaurant);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantDto> updateRestaurant(@PathVariable Long id, 
                                                         @Valid @RequestBody UpdateRestaurantDto updateDto) {
        Optional<RestaurantDto> updatedRestaurant = restaurantService.updateRestaurant(id, updateDto);
        return updatedRestaurant.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        boolean deleted = restaurantService.deleteRestaurant(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getRestaurantCount() {
        long count = restaurantService.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> restaurantExists(@PathVariable Long id) {
        boolean exists = restaurantService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}
