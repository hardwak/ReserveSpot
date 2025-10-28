package com.pwr_zpi.reservespotapi.mapper.picture;

import com.pwr_zpi.reservespotapi.dto.picture.CreatePictureDto;
import com.pwr_zpi.reservespotapi.dto.picture.PictureDto;
import com.pwr_zpi.reservespotapi.dto.picture.UpdatePictureDto;
import com.pwr_zpi.reservespotapi.entities.picture.Picture;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PictureMapper {

    public PictureDto toDto(Picture picture) {
        if (picture == null) {
            return null;
        }

        return PictureDto.builder()
                .id(picture.getId())
                .url(picture.getUrl())
                .uploadedAt(picture.getUploadedAt())
                .description(picture.getDescription())
                .restaurantIds(picture.getRestaurants() != null ? 
                    picture.getRestaurants().stream().map(restaurant -> restaurant.getId()).collect(Collectors.toSet()) : null)
                .reviewIds(picture.getReviews() != null ? 
                    picture.getReviews().stream().map(review -> review.getId()).collect(Collectors.toSet()) : null)
                .build();
    }

    public Picture toEntity(CreatePictureDto dto) {
        if (dto == null) {
            return null;
        }

        return Picture.builder()
                .url(dto.getUrl())
                .description(dto.getDescription())
                .uploadedAt(java.time.LocalDateTime.now())
                .build();
    }

    public void updateEntity(UpdatePictureDto dto, Picture picture) {
        if (dto == null || picture == null) {
            return;
        }

        if (dto.getUrl() != null) {
            picture.setUrl(dto.getUrl());
        }
        if (dto.getDescription() != null) {
            picture.setDescription(dto.getDescription());
        }
    }
}
