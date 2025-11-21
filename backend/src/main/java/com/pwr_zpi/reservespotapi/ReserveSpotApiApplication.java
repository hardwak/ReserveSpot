package com.pwr_zpi.reservespotapi;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReserveSpotApiApplication {

    public static void main(String[] args) {
        // Load .env file before Spring Boot starts
        loadEnvFile();
        
        SpringApplication.run(ReserveSpotApiApplication.class, args);
    }

    private static void loadEnvFile() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMissing()
                    .load();
            
            // Set system properties from .env file
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });
            
            System.out.println("✅ Loaded " + dotenv.entries().size() + " environment variables from .env file");
        } catch (Exception e) {
            System.err.println("⚠️  Warning: Could not load .env file: " + e.getMessage());
        }
    }
}
