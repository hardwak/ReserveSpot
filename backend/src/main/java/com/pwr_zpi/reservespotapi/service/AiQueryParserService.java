package com.pwr_zpi.reservespotapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.AiAnalysis;
import com.pwr_zpi.reservespotapi.entities.ai_analysis.AiAnalysisRepository;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.tag.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiQueryParserService {

    private final ObjectMapper objectMapper;
    private final RestaurantRepository restaurantRepository;
    private final AiAnalysisRepository aiAnalysisRepository;

    @Value("${GEMINI_API_KEY:test-api-key}")
    private String apiKey;

    @Data
    private static class RestaurantContextDto {
        private Long id;
        private String name;
        private String city;
        private String description;
        private String aiSummary;
        private List<String> tags;
    }

    @Data
    public static class AiResponse {
        private List<Long> matchingRestaurantIds;
        private String reasoning;
    }

    @Transactional(readOnly = true)
    public List<Long> findMatchingRestaurantIds(String naturalLanguageQuery) {
        try {
            List<Restaurant> allRestaurants = restaurantRepository.findAll();

            List<RestaurantContextDto> contextList = allRestaurants.stream().map(r -> {
                RestaurantContextDto dto = new RestaurantContextDto();
                dto.setId(r.getId());
                dto.setName(r.getName());
                dto.setCity(r.getCity());
                dto.setDescription(r.getDescription());

                dto.setTags(r.getTags().stream().map(Tag::getName).toList());

                aiAnalysisRepository.findByRestaurantId(r.getId())
                        .ifPresent(analysis -> dto.setAiSummary(analysis.getSummaryText()));

                return dto;
            }).toList();

            String restaurantsJson = objectMapper.writeValueAsString(contextList);

            String systemPrompt = String.format(
                    "You are an intelligent restaurant concierge. " +
                            "I will provide you with a JSON list of restaurants (including their names, descriptions, tags, and AI summaries). " +
                            "Your task is to analyze the User Query and return the IDs of the restaurants that best match the request. " +
                            "Analyze deep semantic meaning (e.g. if user asks for 'date night', look for 'romantic', 'fine dining', 'cozy' in descriptions/summaries). " +
                            "If the user specifies a city in the query, STRICTLY filter by that city. " +
                            "Return a JSON object with this schema: {\"matchingRestaurantIds\": [1, 2, ...], \"reasoning\": \"...\"}. " +
                            "If no restaurants match, return an empty list. " +
                            "Data: %s",
                    restaurantsJson
            );

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", naturalLanguageQuery))
                    )),
                    "systemInstruction", Map.of(
                            "parts", List.of(Map.of("text", systemPrompt))
                    ),
                    "generationConfig", Map.of(
                            "responseMimeType", "application/json"
                    )
            ));

            String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-09-2025:generateContent?key=" + apiKey;

            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException("Gemini API call failed: " + response.body());
                }

                JsonNode geminiResponse = objectMapper.readTree(response.body());

                String jsonText = geminiResponse
                        .path("candidates")
                        .path(0)
                        .path("content")
                        .path("parts")
                        .path(0)
                        .path("text")
                        .asText();

                if (jsonText.isEmpty()) {
                    return Collections.emptyList();
                }

                AiResponse aiResponse = objectMapper.readValue(jsonText, AiResponse.class);
                return aiResponse.getMatchingRestaurantIds() != null ? aiResponse.getMatchingRestaurantIds() : Collections.emptyList();
            }

        } catch (Exception e) {
            System.err.println("AI Search Error: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}