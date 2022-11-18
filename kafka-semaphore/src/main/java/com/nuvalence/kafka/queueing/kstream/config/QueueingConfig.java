package com.nuvalence.kafka.queueing.kstream.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kafka-queueing")
public class QueueingConfig {

    private int defaultMaxInflight;
}
