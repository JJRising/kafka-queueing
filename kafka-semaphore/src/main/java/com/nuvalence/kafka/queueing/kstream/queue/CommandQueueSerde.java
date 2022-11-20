package com.nuvalence.kafka.queueing.kstream.queue;

import com.nuvalence.kafka.queueing.Command;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import org.apache.kafka.common.serialization.*;

import java.util.ArrayList;
import java.util.List;

public class CommandQueueSerde implements Serde<List<Command>> {

    private final SchemaRegistryClient schemaRegistryClient;

    public CommandQueueSerde(SchemaRegistryClient schemaRegistryClient) {
        this.schemaRegistryClient = schemaRegistryClient;
    }

    @Override
    public Serializer<List<Command>> serializer() {
        return new ListSerializer<>(
                new KafkaProtobufSerializer<>(schemaRegistryClient));
    }

    @Override
    public Deserializer<List<Command>> deserializer() {
        //noinspection unchecked
        return new ListDeserializer<>(
                ArrayList.class,
                new KafkaProtobufDeserializer<>(schemaRegistryClient));
    }
}
