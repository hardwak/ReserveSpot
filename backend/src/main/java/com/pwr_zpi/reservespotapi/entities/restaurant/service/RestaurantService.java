package com.pwr_zpi.reservespotapi.entities.restaurant.service;

import com.pwr_zpi.reservespotapi.entities.reservation.Reservation;
import com.pwr_zpi.reservespotapi.entities.reservation.ReservationRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.CreateRestaurantDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.RestaurantDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.RestaurantSearchDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.UpdateRestaurantDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.mapper.RestaurantMapper;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTable;
import com.pwr_zpi.reservespotapi.entities.restaurant_table.RestaurantTableRepository;
import com.pwr_zpi.reservespotapi.entities.tag.Tag;
import com.pwr_zpi.reservespotapi.entities.tag.TagRepository;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import com.pwr_zpi.reservespotapi.service.AiQueryParserService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;
    private final AiQueryParserService aiQueryParser;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository restaurantTableRepository;

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

        // Set tags if provided
        if (createDto.getTagIds() != null && !createDto.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(createDto.getTagIds()));
            restaurant.setTags(tags);
        }

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return restaurantMapper.toDto(savedRestaurant);
    }

    public Optional<RestaurantDto> updateRestaurant(Long id, UpdateRestaurantDto updateDto) {
        return restaurantRepository.findById(id)
                .map(restaurant -> {
                    restaurantMapper.updateEntity(updateDto, restaurant);

                    // Update tags if provided
                    if (updateDto.getTagIds() != null) {
                        Set<Tag> tags = new HashSet<>(tagRepository.findAllById(updateDto.getTagIds()));
                        restaurant.setTags(tags);
                    }

                    Restaurant savedRestaurant = restaurantRepository.save(restaurant);
                    return restaurantMapper.toDto(savedRestaurant);
                });
    }

    public boolean deleteRestaurant(Long id) {
        return restaurantRepository.findById(id)
                .map(restaurant -> {
                    // Get all tables for this restaurant
                    List<RestaurantTable> tables = restaurantTableRepository.findByRestaurantId(id);

                    // Delete all reservations for all tables
                    tables.forEach(table -> {
                        List<Reservation> reservations = reservationRepository.findByTableId(table.getId());
                        if (!reservations.isEmpty()) {
                            reservationRepository.deleteAll(reservations);
                        }
                    });

                    // Now delete the restaurant (cascade will delete tables, reviews, etc.)
                    restaurantRepository.delete(restaurant);
            return true;
                })
                .orElse(false);
    }

    public boolean existsById(Long id) {
        return restaurantRepository.existsById(id);
    }

    public long count() {
        return restaurantRepository.count();
    }

//    public List<RestaurantDto> getRecommendations(Long userId) {
//        int limit = 5;
//        Pageable pageable = PageRequest.of(0, limit);
//        List<Restaurant> recommendations = new ArrayList<>();
//        Set<Long> visitedRestaurantIds = new HashSet<>();
//        Set<Long> preferredTagIds = new HashSet<>();
//
//        if (userId != null) {
//            userRepository.findById(userId).ifPresent(user -> {
//                if (user.getReservations() != null) {
//                    user.getReservations().forEach(res -> {
//                        visitedRestaurantIds.add(res.getTable().getRestaurant().getId());
//                        res.getTable().getRestaurant().getTags().forEach(tag -> preferredTagIds.add(tag.getId()));
//                    });
//                }
//                if (user.getReviews() != null) {
//                    user.getReviews().forEach(review -> {
//                        visitedRestaurantIds.add(review.getRestaurant().getId());
//                        review.getRestaurant().getTags().forEach(tag -> preferredTagIds.add(tag.getId()));
//                    });
//                }
//            });
//        }
//
//        if (!preferredTagIds.isEmpty()) {
//            List<Restaurant> tagBased;
//            if (visitedRestaurantIds.isEmpty()) {
//                tagBased = restaurantRepository.findByTagsIn(preferredTagIds, pageable);
//            } else {
//                tagBased = restaurantRepository.findByTagsInAndIdNotIn(preferredTagIds, visitedRestaurantIds, pageable);
//            }
//            recommendations.addAll(tagBased);
//        }
//
//        if (recommendations.size() < limit) {
//            Pageable topRatedPage = PageRequest.of(0, limit * 2);
//            List<Restaurant> topRated = restaurantRepository.findTopRated(topRatedPage);
//
//            for (Restaurant r : topRated) {
//                if (recommendations.size() >= limit) break;
//                if (!recommendations.contains(r) && !visitedRestaurantIds.contains(r.getId())) {
//                    recommendations.add(r);
//                }
//            }
//        }
//
//        if (recommendations.size() < limit) {
//            int needed = limit - recommendations.size();
//
//            Set<Long> excludedIds = recommendations.stream()
//                    .map(Restaurant::getId)
//                    .collect(Collectors.toSet());
//            excludedIds.addAll(visitedRestaurantIds);
//
//            List<Restaurant> randomRestaurants;
//
//            if (excludedIds.isEmpty()) {
//                randomRestaurants = restaurantRepository.findRandom(needed);
//            } else {
//                randomRestaurants = restaurantRepository.findRandomNotIn(excludedIds, needed);
//            }
//
//            recommendations.addAll(randomRestaurants);
//        }
//
//        if (recommendations.size() < limit) {
//            int needed = limit - recommendations.size();
//            Set<Long> currentRecIds = recommendations.stream().map(Restaurant::getId).collect(Collectors.toSet());
//
//            List<Restaurant> anyRandoms = restaurantRepository.findRandomNotIn(currentRecIds, needed);
//            recommendations.addAll(anyRandoms);
//        }
//
//        return recommendations.stream()
//                .map(restaurantMapper::toDto)
//                .toList();
//    }

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
        List<Long> matchingIds = aiQueryParser.findMatchingRestaurantIds(searchDto.getQuery());

        if (matchingIds.isEmpty()) {
            return List.of();
        }

        List<Restaurant> restaurants = restaurantRepository.findAllById(matchingIds);

        if (searchDto.getCity() != null && !searchDto.getCity().isBlank()) {
            restaurants = restaurants.stream()
                    .filter(r -> r.getCity() != null && r.getCity().equalsIgnoreCase(searchDto.getCity()))
                    .collect(Collectors.toList());
        }

        if (searchDto.getMinRating() != null) {
            restaurants = restaurants.stream()
                    .filter(r -> r.getAverageRating() != null && r.getAverageRating() >= searchDto.getMinRating())
                    .collect(Collectors.toList());
        }

        return restaurants.stream()
                .map(restaurantMapper::toDto)
                .toList();
    }

    public List<String> getAllCities() {
        return restaurantRepository.findAllCities();
    }

    public List<RestaurantDto> getRecommendations(Long userId, String city) {
        int limit = 5;
        Pageable pageable = PageRequest.of(0, limit);

        List<Restaurant> recommendations = new ArrayList<>();
        Set<Long> visitedRestaurantIds = new HashSet<>();
        Set<Long> preferredTagIds = new HashSet<>();

        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                if (user.getReservations() != null) {
                    user.getReservations().forEach(res -> {
                        visitedRestaurantIds.add(res.getTable().getRestaurant().getId());
                        res.getTable().getRestaurant().getTags().forEach(tag -> preferredTagIds.add(tag.getId()));
                    });
                }
                if (user.getReviews() != null) {
                    user.getReviews().forEach(review -> {
                        visitedRestaurantIds.add(review.getRestaurant().getId());
                        review.getRestaurant().getTags().forEach(tag -> preferredTagIds.add(tag.getId()));
                    });
                }
            });
        }

        // 2. STRATEGIA A: PO TAGACH (Content-Based)
        if (!preferredTagIds.isEmpty()) {
            List<Restaurant> tagBased;
            if (city != null && !city.isBlank()) {
                if (visitedRestaurantIds.isEmpty()) {
                    tagBased = restaurantRepository.findByTagsInAndCity(preferredTagIds, city, pageable);
                } else {
                    tagBased = restaurantRepository.findByTagsInAndIdNotInAndCity(preferredTagIds, visitedRestaurantIds, city, pageable);
                }
            } else {
                if (visitedRestaurantIds.isEmpty()) {
                    tagBased = restaurantRepository.findByTagsIn(preferredTagIds, pageable);
                } else {
                    tagBased = restaurantRepository.findByTagsInAndIdNotIn(preferredTagIds, visitedRestaurantIds, pageable);
                }
            }
            recommendations.addAll(tagBased);
        }

        if (recommendations.size() < limit) {
            Pageable topRatedPage = PageRequest.of(0, limit * 2);
            List<Restaurant> topRated;

            if (city != null && !city.isBlank()) {
                topRated = restaurantRepository.findTopRatedByCity(city, topRatedPage);
            } else {
                topRated = restaurantRepository.findTopRated(topRatedPage);
            }

            for (Restaurant r : topRated) {
                if (recommendations.size() >= limit) break;
                if (!recommendations.contains(r) && !visitedRestaurantIds.contains(r.getId())) {
                    recommendations.add(r);
                }
            }
        }

        if (recommendations.size() < limit) {
            int needed = limit - recommendations.size();
            Set<Long> excludedIds = recommendations.stream()
                    .map(Restaurant::getId)
                    .collect(Collectors.toSet());
            excludedIds.addAll(visitedRestaurantIds);

            List<Restaurant> randomRestaurants;

            if (city != null && !city.isBlank()) {
                if (excludedIds.isEmpty()) {
                    randomRestaurants = restaurantRepository.findRandomByCity(city, needed);
                } else {
                    randomRestaurants = restaurantRepository.findRandomNotInAndCity(excludedIds, city, needed);
                }
            } else {
                if (excludedIds.isEmpty()) {
                    randomRestaurants = restaurantRepository.findRandom(needed);
                } else {
                    randomRestaurants = restaurantRepository.findRandomNotIn(excludedIds, needed);
                }
            }
            recommendations.addAll(randomRestaurants);
        }

        if (recommendations.size() < limit) {
            int needed = limit - recommendations.size();
            Set<Long> currentRecIds = recommendations.stream().map(Restaurant::getId).collect(Collectors.toSet());

            List<Restaurant> anyRandoms;
            if (city != null && !city.isBlank()) {
                anyRandoms = restaurantRepository.findRandomNotInAndCity(currentRecIds, city, needed);
            } else {
                anyRandoms = restaurantRepository.findRandomNotIn(currentRecIds, needed);
            }
            recommendations.addAll(anyRandoms);
        }

        return recommendations.stream()
                .map(restaurantMapper::toDto)
                .toList();
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
                .map(Tag::getId)
                .collect(Collectors.toSet());

        return restaurantTagIds.containsAll(tagIds);
    }
}
