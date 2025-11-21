package com.pwr_zpi.reservespotapi.entities.picture.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PictureDto {
    private Long id;
    private String url;
    private LocalDateTime uploadedAt;
    private String description;
    private Set<Long> restaurantIds;
    private Set<Long> reviewIds;
}
