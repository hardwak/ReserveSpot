package com.pwr_zpi.reservespotapi.service.restaurant;

import com.pwr_zpi.reservespotapi.dto.restaurant.CreateRestaurantDto;
import com.pwr_zpi.reservespotapi.dto.restaurant.RestaurantDto;
import com.pwr_zpi.reservespotapi.dto.restaurant.UpdateRestaurantDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.mapper.restaurant.RestaurantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
}
