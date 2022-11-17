package com.nuvalence.kafka.queueing.kstream.schemaRegistry;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "schema-registry")
public class SchemaRegistryConfig {

    private String schemaRegistryBaseUrl;
    private int schemaRegistryClientCacheCapacity = 1000;

    @Bean
    public SchemaRegistryClient schemaRegistryClient(StreamsConfig streamsConfig) {
        return new CachedSchemaRegistryClient(schemaRegistryBaseUrl,
                schemaRegistryClientCacheCapacity,
                Collections.singletonList(new ProtobufSchemaProvider()),
                streamsConfig.originals());
    }
}
