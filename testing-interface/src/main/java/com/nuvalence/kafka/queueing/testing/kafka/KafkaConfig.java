package com.nuvalence.kafka.queueing.testing.kafka;

import com.nuvalence.kafka.queueing.proto.Command;
import com.nuvalence.kafka.queueing.proto.Event;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;
import java.util.UUID;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

@Getter
@Setter
@Configuration
@ConfigurationProperties("kafka")
public class KafkaConfig {

    private String commandRequestTopic;
    private String eventTopic;

    private Map<String, Object> kafkaConfigProps;

    @Bean(name = "schemaRegistryClient")
    public SchemaRegistryClient registryClient() {
        return new CachedSchemaRegistryClient(
                (String) kafkaConfigProps.get(SCHEMA_REGISTRY_URL_CONFIG),
                1000);
    }

    @Bean
    public KafkaTemplate<UUID, Command> commandTemplate(SchemaRegistryClient registryClient) {
        ProducerFactory<UUID, Command>  producerFactory =
                new DefaultKafkaProducerFactory<>(
                        kafkaConfigProps,
                        new UUIDSerializer(),
                        new KafkaProtobufSerializer<>(registryClient));
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<UUID, Event> eventTemplate(SchemaRegistryClient registryClient) {
        ProducerFactory<UUID, Event>  producerFactory =
                new DefaultKafkaProducerFactory<>(
                        kafkaConfigProps,
                        new UUIDSerializer(),
                        new KafkaProtobufSerializer<>(registryClient));
        return new KafkaTemplate<>(producerFactory);
    }
}
