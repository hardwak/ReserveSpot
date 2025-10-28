package com.pwr_zpi.reservespotapi.entities.restaurant.mapper;

import com.pwr_zpi.reservespotapi.entities.restaurant.dto.CreateRestaurantDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.RestaurantDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.dto.UpdateRestaurantDto;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.users.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RestaurantMapper {

    public RestaurantDto toDto(Restaurant restaurant) {
        if (restaurant == null) {
            return null;
        }

        return RestaurantDto.builder()
                .id(restaurant.getId())
                .ownerId(restaurant.getOwner() != null ? restaurant.getOwner().getId() : null)
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .city(restaurant.getCity())
                .description(restaurant.getDescription())
                .openingHours(restaurant.getOpeningHours())
                .averageRating(restaurant.getAverageRating())
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .pic(restaurant.getPic())
                .tableIds(restaurant.getTables() != null ? 
                    restaurant.getTables().stream().map(table -> table.getId()).collect(Collectors.toSet()) : null)
                .reviewIds(restaurant.getReviews() != null ? 
                    restaurant.getReviews().stream().map(review -> review.getId()).collect(Collectors.toSet()) : null)
                .aiAnalysisIds(restaurant.getAiAnalyses() != null ? 
                    restaurant.getAiAnalyses().stream().map(analysis -> analysis.getId()).collect(Collectors.toSet()) : null)
                .statisticIds(restaurant.getStatistics() != null ? 
                    restaurant.getStatistics().stream().map(stat -> stat.getId()).collect(Collectors.toSet()) : null)
                .tagIds(restaurant.getTags() != null ? 
                    restaurant.getTags().stream().map(tag -> tag.getId()).collect(Collectors.toSet()) : null)
                .pictureIds(restaurant.getPictures() != null ? 
                    restaurant.getPictures().stream().map(picture -> picture.getId()).collect(Collectors.toSet()) : null)
                .build();
    }

    public Restaurant toEntity(CreateRestaurantDto dto) {
        if (dto == null) {
            return null;
        }

        Restaurant restaurant = Restaurant.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .city(dto.getCity())
                .description(dto.getDescription())
                .openingHours(dto.getOpeningHours())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .pic(dto.getPic())
                .build();

        // Set owner if provided
        if (dto.getOwnerId() != null) {
            User owner = new User();
            owner.setId(dto.getOwnerId());
            restaurant.setOwner(owner);
        }

        return restaurant;
    }

    public void updateEntity(UpdateRestaurantDto dto, Restaurant restaurant) {
        if (dto == null || restaurant == null) {
            return;
        }

        if (dto.getName() != null) {
            restaurant.setName(dto.getName());
        }
        if (dto.getAddress() != null) {
            restaurant.setAddress(dto.getAddress());
        }
        if (dto.getCity() != null) {
            restaurant.setCity(dto.getCity());
        }
        if (dto.getDescription() != null) {
            restaurant.setDescription(dto.getDescription());
        }
        if (dto.getOpeningHours() != null) {
            restaurant.setOpeningHours(dto.getOpeningHours());
        }
        if (dto.getLatitude() != null) {
            restaurant.setLatitude(dto.getLatitude());
        }
        if (dto.getLongitude() != null) {
            restaurant.setLongitude(dto.getLongitude());
        }
        if (dto.getPic() != null) {
            restaurant.setPic(dto.getPic());
        }
    }
}
