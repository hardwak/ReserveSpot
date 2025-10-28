package com.pwr_zpi.reservespotapi.dto.ai_analysis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateAiAnalysisDto {
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
    
    private String summaryText;
    private Double sentimentScore;
}
