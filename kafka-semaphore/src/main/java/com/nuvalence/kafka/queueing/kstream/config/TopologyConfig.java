package com.nuvalence.kafka.queueing.kstream.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kafka-topology")
public class TopologyConfig {

    private String inputTopicName;
    private String eventTopicName;
    private String outputTopicName;

    private Map<String, String> properties = new HashMap<>();

    private StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse streamThreadExceptionResponse =
            StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;

    @Bean(name = "queueStreamConfig")
    public StreamsConfig streamsConfig() {
        return new StreamsConfig(Stream.of(properties).collect(Properties::new, Map::putAll, Map::putAll));
    }
}
