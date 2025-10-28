package com.pwr_zpi.reservespotapi.dto.ai_analysis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AiAnalysisDto {
    private Long id;
    private Long restaurantId;
    private String summaryText;
    private Double sentimentScore;
    private LocalDateTime lastUpdated;
}
