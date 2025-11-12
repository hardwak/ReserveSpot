package com.pwr_zpi.reservespotapi.entities.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEligibilityResponse {
    private boolean canReview;
    private String message;
}

