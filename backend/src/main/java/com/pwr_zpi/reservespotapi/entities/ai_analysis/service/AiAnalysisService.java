package com.pwr_zpi.reservespotapi.entities.ai_analysis.service;

import com.pwr_zpi.reservespotapi.entities.ai_analysis.dto.AiAnalysisDto;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.dto.CreateAiAnalysisDto;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.dto.UpdateAiAnalysisDto;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.AiAnalysis;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.AiAnalysisRepository;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.mapper.AiAnalysisMapper;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.RestaurantStatisticRepository;
import com.pwr_zpi.reservespotapi.entities.review.ReviewRepository;
import com.pwr_zpi.reservespotapi.service.RestaurantAiAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiAnalysisService {

    private final AiAnalysisRepository analysisRepository;
    private final AiAnalysisMapper analysisMapper;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final RestaurantStatisticRepository statisticRepository;
    private final RestaurantAiAnalysisService restaurantAiAnalysisService;

    public List<AiAnalysisDto> getAllAnalyses() {
        return analysisRepository.findAll()
                .stream()
                .map(analysisMapper::toDto)
                .toList();
    }

    public Optional<AiAnalysisDto> getAnalysisById(Long id) {
        return analysisRepository.findById(id)
                .map(analysisMapper::toDto);
    }

    public Optional<AiAnalysisDto> getAnalysisByRestaurantId(Long restaurantId) {
        return analysisRepository.findByRestaurantId(restaurantId)
                .map(analysisMapper::toDto);
    }

    public AiAnalysisDto createAnalysis(CreateAiAnalysisDto createDto) {
        AiAnalysis analysis = analysisMapper.toEntity(createDto);
        AiAnalysis savedAnalysis = analysisRepository.save(analysis);
        return analysisMapper.toDto(savedAnalysis);
    }

    public Optional<AiAnalysisDto> updateAnalysis(Long id, UpdateAiAnalysisDto updateDto) {
        return analysisRepository.findById(id)
                .map(analysis -> {
                    analysisMapper.updateEntity(updateDto, analysis);
                    AiAnalysis savedAnalysis = analysisRepository.save(analysis);
                    return analysisMapper.toDto(savedAnalysis);
                });
    }

    public boolean deleteAnalysis(Long id) {
        if (analysisRepository.existsById(id)) {
            analysisRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean existsById(Long id) {
        return analysisRepository.existsById(id);
    }

    public long count() {
        return analysisRepository.count();
    }

    /**
     * Generate or update AI analysis for a restaurant based on its description, reviews, and statistics
     */
    public AiAnalysisDto generateAnalysisForRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + restaurantId));

        // Get all reviews for the restaurant
        List<com.pwr_zpi.reservespotapi.entities.review.Review> reviews = reviewRepository.findByRestaurantId(restaurantId);

        // Generate AI analysis
        RestaurantAiAnalysisService.AnalysisResult analysisResult = 
                restaurantAiAnalysisService.analyzeRestaurant(restaurant, reviews);

        // Check if analysis already exists
        Optional<AiAnalysis> existingAnalysis = analysisRepository.findByRestaurantId(restaurantId);

        if (existingAnalysis.isPresent()) {
            // Update existing analysis
            AiAnalysis analysis = existingAnalysis.get();
            analysis.setSummaryText(analysisResult.getSummaryText());
            analysis.setSentimentScore(analysisResult.getSentimentScore());
            analysis.setLastUpdated(LocalDateTime.now());
            AiAnalysis savedAnalysis = analysisRepository.save(analysis);
            return analysisMapper.toDto(savedAnalysis);
        } else {
            // Create new analysis
            AiAnalysis newAnalysis = AiAnalysis.builder()
                    .restaurant(restaurant)
                    .summaryText(analysisResult.getSummaryText())
                    .sentimentScore(analysisResult.getSentimentScore())
                    .lastUpdated(LocalDateTime.now())
                    .build();
            AiAnalysis savedAnalysis = analysisRepository.save(newAnalysis);
            return analysisMapper.toDto(savedAnalysis);
        }
    }

    /**
     * Generate AI analysis for all restaurants
     */
    public void generateAnalysisForAllRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        for (Restaurant restaurant : restaurants) {
            try {
                generateAnalysisForRestaurant(restaurant.getId());
            } catch (Exception e) {
                System.err.println("Failed to generate analysis for restaurant " + restaurant.getId() + ": " + e.getMessage());
            }
        }
    }
}
