package com.pwr_zpi.reservespotapi.entities.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateReviewDto {
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    private Long reservationId;
    
    @Pattern(
            regexp = "^$|^\\+?[0-9\\s-]{7,15}$",
            message = "Phone number must contain 7-15 digits and may start with '+'"
    )
    private String phoneNumber;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
    
    private String comment;
    private String pic;
}
