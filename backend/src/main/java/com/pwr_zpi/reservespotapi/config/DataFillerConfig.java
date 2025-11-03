package com.pwr_zpi.reservespotapi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

//@Configuration
@RequiredArgsConstructor
public class DataFillerConfig {

    //@Bean
    public CommandLineRunner dataFiller() {
        return (args) -> {

        };
    }
}
