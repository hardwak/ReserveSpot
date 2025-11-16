package com.pwr_zpi.reservespotapi.config;

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
}
