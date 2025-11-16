package com.pwr_zpi.reservespotapi.config;

import com.pwr_zpi.reservespotapi.entities.ai_analysis.service.AiAnalysisService;
import com.pwr_zpi.reservespotapi.entities.restaurant.RestaurantRepository;
import com.pwr_zpi.reservespotapi.entities.tag.Tag;
import com.pwr_zpi.reservespotapi.entities.tag.TagRepository;
import org.springframework.beans.factory.annotation.Value;
import com.pwr_zpi.reservespotapi.entities.users.Role;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataFillerConfig {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final RestaurantRepository restaurantRepository;
    private final AiAnalysisService aiAnalysisService;

    @Value("${ADMIN_USERNAME}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Bean
    public CommandLineRunner dataFiller() {
        return (args) -> {
            userRepository.save(
                    User.builder()
                            .email(adminUsername)
                            .passwordHash(passwordEncoder.encode(adminPassword))
                            .role(Role.ADMIN)
                            .build()
            );
            userRepository.save(
                    User.builder()
                            .email("user")
                            .passwordHash(passwordEncoder.encode("user"))
                            .role(Role.CLIENT)
                            .build()
            );

            Tag tagItalian = tagRepository.save(Tag.builder().name("Italian").build());
            Tag tagBar = tagRepository.save(Tag.builder().name("Bar").build());
            Tag tagBreakfast = tagRepository.save(Tag.builder().name("Breakfast").build());
            Tag tagMexican = tagRepository.save(Tag.builder().name("Mexican").build());
            Tag tagFrench = tagRepository.save(Tag.builder().name("French").build());
            Tag tagIndian = tagRepository.save(Tag.builder().name("Indian").build());
            Tag tagGreek = tagRepository.save(Tag.builder().name("Greek").build());
            Tag tagSpanish = tagRepository.save(Tag.builder().name("Spanish").build());
            Tag tagVegetarian = tagRepository.save(Tag.builder().name("Vegetarian").build());
            Tag tagGlutenFree = tagRepository.save(Tag.builder().name("Gluten-Free").build());
            Tag tagSeafood = tagRepository.save(Tag.builder().name("Seafood").build());
            Tag tagSteakhouse = tagRepository.save(Tag.builder().name("Steakhouse").build());
            Tag tagRomantic = tagRepository.save(Tag.builder().name("Romantic").build());
            Tag tagFamilyFriendly = tagRepository.save(Tag.builder().name("Family-Friendly").build());
            Tag tagOutdoorSeating = tagRepository.save(Tag.builder().name("Outdoor Seating").build());
            Tag tagPetFriendly = tagRepository.save(Tag.builder().name("Pet-Friendly").build());
            Tag tagCheapEats = tagRepository.save(Tag.builder().name("Cheap Eats").build());
            Tag tagBrunch = tagRepository.save(Tag.builder().name("Brunch").build());
        };
    }

    @Bean
    public CommandLineRunner aiAnalysisGenerator() {
        return (args) -> {
            // Generate AI analysis for all restaurants that don't have one yet
            // This runs after the application starts
            try {
                System.out.println("🤖 Generating AI analysis for restaurants...");
                restaurantRepository.findAll().forEach(restaurant -> {
                    try {
                        // Only generate if analysis doesn't exist
                        if (aiAnalysisService.getAnalysisByRestaurantId(restaurant.getId()).isEmpty()) {
                            aiAnalysisService.generateAnalysisForRestaurant(restaurant.getId());
                            System.out.println("✅ Generated AI analysis for restaurant: " + restaurant.getName());
                        } else {
                            System.out.println("⏭️  AI analysis already exists for restaurant: " + restaurant.getName());
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Failed to generate AI analysis for restaurant " + restaurant.getId() + ": " + e.getMessage());
                    }
                });
                System.out.println("✅ AI analysis generation completed!");
            } catch (Exception e) {
                System.err.println("❌ Error during AI analysis generation: " + e.getMessage());
            }
        };
    }
}
