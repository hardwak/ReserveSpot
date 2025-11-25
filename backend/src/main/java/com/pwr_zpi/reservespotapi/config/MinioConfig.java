package com.pwr_zpi.reservespotapi.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${MINIO_URL}")
    private String url;

    @Value("${MINIO_ROOT_USER}")
    private String root_user;

    @Value("${MINIO_ROOT_PASSWORD}")
    private String root_password;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(url)
                .credentials(root_user, root_password)
                .build();
    }
}