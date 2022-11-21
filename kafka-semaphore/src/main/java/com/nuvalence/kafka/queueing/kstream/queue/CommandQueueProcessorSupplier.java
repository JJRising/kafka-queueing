package com.nuvalence.kafka.queueing.kstream.queue;

import com.nuvalence.kafka.queueing.kstream.semaphore.Semaphore;
import com.nuvalence.kafka.queueing.kstream.semaphore.SemaphoreConfig;
import com.nuvalence.kafka.queueing.proto.Command;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CommandQueueProcessorSupplier implements ProcessorSupplier<UUID, Command, UUID, Command> {

    public static final String COMMAND_QUEUE = "command-queue";

    private final QueueingConfig queueingConfig;
    private final SemaphoreConfig semaphoreConfig;
    private final SchemaRegistryClient schemaRegistryClient;
    private final StreamsConfig streamsConfig;

    public CommandQueueProcessorSupplier(QueueingConfig queueingConfig,
                                         SemaphoreConfig semaphoreConfig,
                                         SchemaRegistryClient schemaRegistryClient,
                                         StreamsConfig streamsConfig) {
        this.queueingConfig = queueingConfig;
        this.semaphoreConfig = semaphoreConfig;
        this.schemaRegistryClient = schemaRegistryClient;
        this.streamsConfig = streamsConfig;
    }

    @Override
    public Processor<UUID, Command, UUID, Command> get() {
        return new CommandQueueProcessor(new Semaphore(semaphoreConfig.getSemaphoreLimits()), queueingConfig);
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        Serde<Command> innerSerde = new KafkaProtobufSerde<>(schemaRegistryClient, Command.class);
        innerSerde.configure(streamsConfig.originals(), false);
        @SuppressWarnings("unchecked")
        StoreBuilder<KeyValueStore<UUID, List<Command>>> keyValueStoreStoreBuilder =
                Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore(COMMAND_QUEUE),
                        Serdes.UUID(),
                        Serdes.ListSerde(ArrayList.class, innerSerde));
        return Collections.singleton(keyValueStoreStoreBuilder);
    }
}
