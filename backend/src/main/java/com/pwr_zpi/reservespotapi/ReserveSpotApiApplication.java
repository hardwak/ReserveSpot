package com.pwr_zpi.reservespotapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReserveSpotApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReserveSpotApiApplication.class, args);
    }
}
