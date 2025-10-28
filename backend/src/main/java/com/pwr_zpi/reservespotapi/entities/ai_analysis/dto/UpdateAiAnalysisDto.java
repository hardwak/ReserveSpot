package com.pwr_zpi.reservespotapi.entities.ai_analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdateAiAnalysisDto {
    private String summaryText;
    private Double sentimentScore;
}
