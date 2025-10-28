package com.pwr_zpi.reservespotapi.dto.ai_analysis;

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
