package com.pwr_zpi.reservespotapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.pwr_zpi.reservespotapi.entities.restaurant.Restaurant;
import com.pwr_zpi.reservespotapi.entities.restaurant_statistic.RestaurantStatistic;
import com.pwr_zpi.reservespotapi.entities.review.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantAiAnalysisService {

    private final ObjectMapper objectMapper;

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    public static class AnalysisResult {
        private String summaryText;
        private Double sentimentScore;

        public AnalysisResult(String summaryText, Double sentimentScore) {
            this.summaryText = summaryText;
            this.sentimentScore = sentimentScore;
        }

        public String getSummaryText() {
            return summaryText;
        }

        public Double getSentimentScore() {
            return sentimentScore;
        }
    }

    public AnalysisResult analyzeRestaurant(Restaurant restaurant, List<Review> reviews, List<RestaurantStatistic> statistics) {
        try {
            // Build context from restaurant description
            String restaurantInfo = String.format(
                    "Restaurant: %s\nDescription: %s\nLocation: %s, %s\nAverage Rating: %.1f",
                    restaurant.getName(),
                    restaurant.getDescription() != null ? restaurant.getDescription() : "No description available",
                    restaurant.getCity() != null ? restaurant.getCity() : "Unknown",
                    restaurant.getAddress() != null ? restaurant.getAddress() : "",
                    restaurant.getAverageRating() != null ? restaurant.getAverageRating() : 0.0
            );

            // Build reviews context
            String reviewsContext;
            if (reviews == null || reviews.isEmpty()) {
                reviewsContext = "No reviews available yet.";
            } else {
                reviewsContext = "Customer Reviews:\n" + reviews.stream()
                        .map(review -> String.format(
                                "- Rating: %d/5\n  Comment: %s",
                                review.getRating() != null ? review.getRating() : 0,
                                review.getComment() != null ? review.getComment() : "No comment"
                        ))
                        .collect(Collectors.joining("\n\n"));
            }

            // Build statistics context
            String statisticsContext;
            if (statistics == null || statistics.isEmpty()) {
                statisticsContext = "No occupancy statistics available yet.";
            } else {
                double avgOccupancy = statistics.stream()
                        .filter(s -> s.getAverageOccupancy() != null)
                        .mapToDouble(RestaurantStatistic::getAverageOccupancy)
                        .average()
                        .orElse(0.0);

                statisticsContext = String.format(
                        "Occupancy Statistics:\n- Average Occupancy: %.1f%%\n- Total Data Points: %d",
                        avgOccupancy * 100,
                        statistics.size()
                );
            }

            String fullContext = String.format(
                    "%s\n\n%s\n\n%s",
                    restaurantInfo,
                    reviewsContext,
                    statisticsContext
            );

            String systemPrompt = "You are a restaurant business analyst. Analyze the provided restaurant information, customer reviews, and occupancy statistics. " +
                    "Generate a comprehensive analysis in JSON format with this exact structure: " +
                    "{\"summaryText\": \"<detailed analysis text>\", \"sentimentScore\": <number between 0.0 and 1.0>}. " +
                    "summaryText: Write a comprehensive 2-3 sentence analysis covering: " +
                    "1. Overall customer sentiment based on reviews " +
                    "2. Key strengths and weaknesses mentioned " +
                    "3. Business performance insights from occupancy data " +
                    "4. Recommendations if applicable. " +
                    "sentimentScore: Calculate based on review ratings and comments (0.0 = very negative, 0.5 = neutral, 1.0 = very positive). " +
                    "If no reviews exist, base sentiment on description and statistics. " +
                    "Be objective and professional in your analysis.";

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", fullContext))
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
                    throw new RuntimeException("Gemini API returned an empty text part.");
                }

                JsonNode analysisJson = objectMapper.readTree(jsonText);
                String summary = analysisJson.path("summaryText").asText();
                Double sentiment = analysisJson.path("sentimentScore").asDouble();

                // Fallback if parsing fails
                if (summary.isEmpty()) {
                    summary = generateFallbackSummary(restaurant, reviews, statistics);
                }
                if (sentiment == null || sentiment.isNaN()) {
                    sentiment = calculateFallbackSentiment(restaurant, reviews);
                }

                return new AnalysisResult(summary, sentiment);

            }

        } catch (Exception e) {
            System.err.println("Restaurant AI analysis error: " + e.getMessage());
            e.printStackTrace();
            // Return fallback analysis
            return new AnalysisResult(
                    generateFallbackSummary(restaurant, reviews, statistics),
                    calculateFallbackSentiment(restaurant, reviews)
            );
        }
    }

    private String generateFallbackSummary(Restaurant restaurant, List<Review> reviews, List<RestaurantStatistic> statistics) {
        StringBuilder summary = new StringBuilder();
        summary.append(restaurant.getName()).append(" is a restaurant");

        if (restaurant.getDescription() != null && !restaurant.getDescription().isEmpty()) {
            summary.append(" offering ").append(restaurant.getDescription().substring(0, Math.min(50, restaurant.getDescription().length())));
        }

        if (reviews != null && !reviews.isEmpty()) {
            double avgRating = reviews.stream()
                    .filter(r -> r.getRating() != null)
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            summary.append(". Based on ").append(reviews.size()).append(" review(s) with an average rating of ").append(String.format("%.1f", avgRating));
        }

        if (statistics != null && !statistics.isEmpty()) {
            double avgOccupancy = statistics.stream()
                    .filter(s -> s.getAverageOccupancy() != null)
                    .mapToDouble(RestaurantStatistic::getAverageOccupancy)
                    .average()
                    .orElse(0.0);
            summary.append(". Average occupancy rate is ").append(String.format("%.1f%%", avgOccupancy * 100));
        }

        summary.append(".");
        return summary.toString();
    }

    private Double calculateFallbackSentiment(Restaurant restaurant, List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            // Base sentiment on average rating if available
            if (restaurant.getAverageRating() != null) {
                return restaurant.getAverageRating() / 5.0;
            }
            return 0.5; // Neutral if no data
        }

        // Calculate average rating from reviews
        double avgRating = reviews.stream()
                .filter(r -> r.getRating() != null)
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // Convert 1-5 scale to 0.0-1.0 sentiment
        return (avgRating - 1.0) / 4.0;
    }
}

