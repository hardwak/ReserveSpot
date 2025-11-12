package com.pwr_zpi.reservespotapi.entities.ai_analysis.mapper;

import com.pwr_zpi.reservespotapi.entities.ai_analysis.dto.AiAnalysisDto;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.dto.CreateAiAnalysisDto;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.dto.UpdateAiAnalysisDto;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.AiAnalysis;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import org.springframework.stereotype.Component;

@Component
public class AiAnalysisMapper {

    public AiAnalysisDto toDto(AiAnalysis analysis) {
        if (analysis == null) {
            return null;
        }

        return AiAnalysisDto.builder()
                .id(analysis.getId())
                .restaurantId(analysis.getRestaurant() != null ? analysis.getRestaurant().getId() : null)
                .summaryText(analysis.getSummaryText())
                .sentimentScore(analysis.getSentimentScore())
                .lastUpdated(analysis.getLastUpdated())
                .build();
    }

    public AiAnalysis toEntity(CreateAiAnalysisDto dto) {
        if (dto == null) {
            return null;
        }

        AiAnalysis analysis = AiAnalysis.builder()
                .summaryText(dto.getSummaryText())
                .sentimentScore(dto.getSentimentScore())
                .lastUpdated(java.time.LocalDateTime.now())
                .build();

        // Set restaurant if provided
        if (dto.getRestaurantId() != null) {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(dto.getRestaurantId());
            analysis.setRestaurant(restaurant);
        }

        return analysis;
    }

    public void updateEntity(UpdateAiAnalysisDto dto, AiAnalysis analysis) {
        if (dto == null || analysis == null) {
            return;
        }

        if (dto.getSummaryText() != null) {
            analysis.setSummaryText(dto.getSummaryText());
        }
        if (dto.getSentimentScore() != null) {
            analysis.setSentimentScore(dto.getSentimentScore());
        }
        analysis.setLastUpdated(java.time.LocalDateTime.now());
    }
}
