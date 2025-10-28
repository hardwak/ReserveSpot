package com.pwr_zpi.reservespotapi.entities.review.dto;

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
public class ReviewDto {
    private Long id;
    private Long userId;
    private Long restaurantId;
    private String phoneNumber;
    private Integer rating;
    private String comment;
    private String pic;
    private LocalDateTime createdAt;
    private Set<Long> pictureIds;
}
