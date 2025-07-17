package com.lesson.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import com.lesson.config.StorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "minio")
public class MinioConfig {

    @Bean
    public MinioClient minioClient(StorageProperties storageProperties) {
        return MinioClient.builder()
                .endpoint(storageProperties.getMinio().getEndpoint())
                .credentials(storageProperties.getMinio().getAccessKey(), storageProperties.getMinio().getSecretKey())
                .build();
    }
} 