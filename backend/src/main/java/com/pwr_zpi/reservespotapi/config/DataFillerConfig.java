package com.pwr_zpi.reservespotapi.config;

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
        };
    }
}
