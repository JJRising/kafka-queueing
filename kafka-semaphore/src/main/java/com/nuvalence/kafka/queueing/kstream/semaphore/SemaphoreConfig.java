package com.nuvalence.kafka.queueing.kstream.semaphore;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "semaphore")
public class SemaphoreConfig {

    private Map<UUID, Integer> semaphoreLimits;

    @Bean
    public Map<UUID, Boolean> semaphoreReleaseMap() {
        return new HashMap<>();
    }
}
