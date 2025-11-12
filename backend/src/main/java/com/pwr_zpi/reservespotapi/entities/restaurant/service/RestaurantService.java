package com.pwr_zpi.reservespotapi.entities.restaurant.service;

import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.CreateRestaurantDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.RestaurantDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.RestaurantSearchDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.UpdateRestaurantDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.mapper.RestaurantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;

    public List<RestaurantDto> getAllRestaurants() {
        return restaurantRepository.findAll()
                .stream()
                .map(restaurantMapper::toDto)
                .toList();
    }

    public Optional<RestaurantDto> getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .map(restaurantMapper::toDto);
    }

    public List<RestaurantDto> getRestaurantsByOwnerId(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId)
                .stream()
                .map(restaurantMapper::toDto)
                .toList();
    }

    public List<RestaurantDto> getRestaurantsByCity(String city) {
        return restaurantRepository.findByCityIgnoreCase(city)
                .stream()
                .map(restaurantMapper::toDto)
                .toList();
    }

    public RestaurantDto createRestaurant(CreateRestaurantDto createDto) {
        Restaurant restaurant = restaurantMapper.toEntity(createDto);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return restaurantMapper.toDto(savedRestaurant);
    }

    public Optional<RestaurantDto> updateRestaurant(Long id, UpdateRestaurantDto updateDto) {
        return restaurantRepository.findById(id)
                .map(restaurant -> {
                    restaurantMapper.updateEntity(updateDto, restaurant);
                    Restaurant savedRestaurant = restaurantRepository.save(restaurant);
                    return restaurantMapper.toDto(savedRestaurant);
                });
    }

    public boolean deleteRestaurant(Long id) {
        if (restaurantRepository.existsById(id)) {
            restaurantRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean existsById(Long id) {
        return restaurantRepository.existsById(id);
    }

    public long count() {
        return restaurantRepository.count();
    }

    public List<RestaurantDto> searchRestaurants(RestaurantSearchDto searchDto) {
        return restaurantRepository.findAll().stream()
                .filter(restaurant -> matchesQuery(restaurant, searchDto.getQuery()))
                .filter(restaurant -> matchesCity(restaurant, searchDto.getCity()))
                .filter(restaurant -> matchesRating(restaurant, searchDto.getMinRating(), searchDto.getMaxRating()))
                .filter(restaurant -> matchesTags(restaurant, searchDto.getTagIds()))
                .map(restaurantMapper::toDto)
                .toList();
    }

    public List<RestaurantDto> searchRestaurantsWithAi(RestaurantSearchDto searchDto) {
        // Placeholder for AI-driven recommendations. For now we reuse the standard search.
        return searchRestaurants(searchDto);
    }

    private boolean matchesQuery(Restaurant restaurant, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normalized = query.toLowerCase();
        return (restaurant.getName() != null && restaurant.getName().toLowerCase().contains(normalized))
                || (restaurant.getDescription() != null && restaurant.getDescription().toLowerCase().contains(normalized));
    }

    private boolean matchesCity(Restaurant restaurant, String city) {
        if (city == null || city.isBlank()) {
            return true;
        }
        return restaurant.getCity() != null && restaurant.getCity().equalsIgnoreCase(city);
    }

    private boolean matchesRating(Restaurant restaurant, Double minRating, Double maxRating) {
        Double rating = restaurant.getAverageRating();

        if (minRating != null && (rating == null || rating < minRating)) {
            return false;
        }

        if (maxRating != null && (rating == null || rating > maxRating)) {
            return false;
        }

        return true;
    }

    private boolean matchesTags(Restaurant restaurant, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return true;
        }

        if (restaurant.getTags() == null || restaurant.getTags().isEmpty()) {
            return false;
        }

        Set<Long> restaurantTagIds = restaurant.getTags().stream()
                .map(tag -> tag.getId())
                .collect(Collectors.toSet());

        return restaurantTagIds.containsAll(tagIds);
    }
}
