package com.nuvalence.kafka.queueing.kstream.queue;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kafka-queueing")
public class QueueingConfig {

    private int defaultSemaphoreLimit = 1;

    private Map<UUID, Integer> semaphoreLimits = new HashMap<>();
}
