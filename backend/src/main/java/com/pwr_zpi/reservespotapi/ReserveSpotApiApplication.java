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
            // Look for .env file in the backend directory (where the application runs from)
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .filename(".env")
                    .ignoreIfMissing()
                    .load();
            
            // Set all .env variables as system properties so Spring can access them
            int count = 0;
            for (io.github.cdimascio.dotenv.DotenvEntry entry : dotenv.entries()) {
                if (System.getProperty(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                    count++;
                }
            }
            
            System.out.println("✅ Loaded " + count + " environment variables from .env file");
        } catch (Exception e) {
            System.out.println("⚠️  Could not load .env file: " + e.getMessage());
            System.out.println("   Using system environment variables instead");
        }
    }
}
