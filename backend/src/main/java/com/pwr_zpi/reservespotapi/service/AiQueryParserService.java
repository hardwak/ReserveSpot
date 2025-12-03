package com.pwr_zpi.reservespotapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.pwr_zpi.reservespotapi.entities.tag.Tag;
import com.pwr_zpi.reservespotapi.entities.tag.TagRepository;
import lombok.Data;
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
public class AiQueryParserService {

    private final ObjectMapper objectMapper;
    private final TagRepository tagRepository;

    @Value("${GEMINI_API_KEY:test-api-key}")
    private String apiKey;

    @Data
    public static class AiParsedCriteria {
        private String impliedQuery;
        private List<String> impliedTags;
        private Double impliedMinRating;
    }

    public AiParsedCriteria parseQuery(String naturalLanguageQuery) {
        try {
            List<Tag> allTags = tagRepository.findAll();
            String availableTags = allTags.stream()
                    .map(Tag::getName)
                    .collect(Collectors.joining(", "));

            String systemPrompt = String.format(
                    "You are a query analyst for a restaurant reservation application. " +
                            "The available tags in the database are: [%s]. " +
                            "Your task is to analyze the user's query and return a JSON object based on their intent. " +
                            "The JSON schema must be as follows: " +
                            "{\"impliedQuery\": \"...\", \"impliedTags\": [\"...\"], \"impliedMinRating\": ...}. " +
                            "impliedQuery: Main keywords (e.g., 'ramen', 'pizza'). " +
                            "impliedTags: A list of tags selected EXCLUSIVELY from the provided list of available tags that match the query. If none match, return an empty list []. " +
                            "impliedMinRating: If the user suggests quality ('good', 'great', 'best'), set 4.0. If they suggest luxury ('exclusive', 'top'), set 4.5. Otherwise, null.",
                    availableTags
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

                // Struktura: { "candidates": [ { "content": { "parts": [ { "text": "..." } ] } } ] }
                String jsonText = geminiResponse
                        .path("candidates") // Przejdź do "candidates" (to jest tablica)
                        .path(0)            // Weź pierwszy element [0]
                        .path("content")    // Przejdź do "content"
                        .path("parts")      // Przejdź do "parts" (to jest tablica)
                        .path(0)            // Weź pierwszy element [0]
                        .path("text")       // Przejdź do "text"
                        .asText();          // Pobierz jako tekst

                if (jsonText.isEmpty()) {
                    throw new RuntimeException("Gemini API returned an empty text part.");
                }

                return objectMapper.readValue(jsonText, AiParsedCriteria.class);

            }

        } catch (Exception e) {
            System.err.println("AI parsing error: " + e.getMessage());
            return new AiParsedCriteria();
        }
    }
}