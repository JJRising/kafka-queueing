package com.nuvalence.kafka.queueing.kstream.topology;

import com.nuvalence.kafka.queueing.kstream.config.TopologyConfig;
import com.nuvalence.kafka.queueing.kstream.delivery.DeliveryProcessorSupplier;
import com.nuvalence.kafka.queueing.kstream.queue.CommandQueueProcessorSupplier;
import com.nuvalence.kafka.queueing.kstream.queue.SemaphoreProcessorSupplier;
import com.nuvalence.kafka.queueing.kstream.validation.ValidationProcessorSupplier;
import com.nuvalence.kafka.queueing.proto.Command;
import com.nuvalence.kafka.queueing.proto.Event;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class QueueStreamTopologyBuilder {

    public static final String COMMAND_SOURCE = "command-source";
    public static final String COMMAND_VALIDATION = "command-validation-processor";
    public static final String SEMAPHORE_ACQUIRE_PROCESSOR = "command-queue-processor";
    public static final String COMMAND_DELIVERY = "command-delivery-processor";
    public static final String COMMAND_SINK = "command-sink";
    public static final String SEMAPHORE_RELEASE_SOURCE = "semaphore-release-source";
    public static final String SEMAPHORE_RELEASE_PROCESSOR = "semaphore-release-processor";
    public static final String COMMAND_QUEUE = "command-queue-store";
    public static final String SEMAPHORE_STORE = "semaphore-store";

    private final TopologyConfig topologyConfig;
    private final StreamsConfig streamsConfig;
    private final SchemaRegistryClient schemaRegistryClient;

    private final ValidationProcessorSupplier commandValidationProcessorSupplier;
    private final CommandQueueProcessorSupplier commandQueueProcessorSupplier;
    private final DeliveryProcessorSupplier commandDeliveryProcessorSupplier;
    private final SemaphoreProcessorSupplier semaphoreProcessorSupplier;

    public QueueStreamTopologyBuilder(TopologyConfig topologyConfig,
                                      StreamsConfig streamsConfig,
                                      SchemaRegistryClient schemaRegistryClient,
                                      ValidationProcessorSupplier commandValidationProcessorSupplier,
                                      CommandQueueProcessorSupplier commandQueueProcessorSupplier,
                                      DeliveryProcessorSupplier commandDeliveryProcessorSupplier,
                                      SemaphoreProcessorSupplier semaphoreProcessorSupplier) {
        this.topologyConfig = topologyConfig;
        this.streamsConfig = streamsConfig;
        this.schemaRegistryClient = schemaRegistryClient;
        this.commandValidationProcessorSupplier = commandValidationProcessorSupplier;
        this.commandQueueProcessorSupplier = commandQueueProcessorSupplier;
        this.commandDeliveryProcessorSupplier = commandDeliveryProcessorSupplier;
        this.semaphoreProcessorSupplier = semaphoreProcessorSupplier;
    }

    public Topology constructQueueTopology() {
        return new Topology()
                .addSource(COMMAND_SOURCE,
                        new UUIDDeserializer(),
                        new KafkaProtobufDeserializer<>(schemaRegistryClient, streamsConfig.originals(), Command.class),
                        topologyConfig.getInputTopicName())
                .addSource(SEMAPHORE_RELEASE_SOURCE,
                        new UUIDDeserializer(),
                        new KafkaProtobufDeserializer<>(schemaRegistryClient, streamsConfig.originals(), Event.class),
                        topologyConfig.getEventTopicName())
                .addProcessor(COMMAND_VALIDATION, commandValidationProcessorSupplier, COMMAND_SOURCE)
                .addProcessor(SEMAPHORE_ACQUIRE_PROCESSOR, commandQueueProcessorSupplier, COMMAND_VALIDATION)
                .addProcessor(SEMAPHORE_RELEASE_PROCESSOR, semaphoreProcessorSupplier, SEMAPHORE_RELEASE_SOURCE)
                .addStateStore(commandQueue())
                .addStateStore(semaphoreStore())
                .connectProcessorAndStateStores(SEMAPHORE_ACQUIRE_PROCESSOR, COMMAND_QUEUE, SEMAPHORE_STORE)
                .connectProcessorAndStateStores(SEMAPHORE_RELEASE_PROCESSOR, COMMAND_QUEUE, SEMAPHORE_STORE)
                .addProcessor(COMMAND_DELIVERY, commandDeliveryProcessorSupplier,
                        SEMAPHORE_ACQUIRE_PROCESSOR, SEMAPHORE_RELEASE_PROCESSOR)
                .addSink(COMMAND_SINK,
                        topologyConfig.getOutputTopicName(),
                        new UUIDSerializer(),
                        new KafkaProtobufSerializer<Command>(schemaRegistryClient, streamsConfig.originals()),
                        COMMAND_DELIVERY);
    }

    @SuppressWarnings("unchecked") // ArrayList has no inner type that can be anticipated
    private StoreBuilder<KeyValueStore<UUID, List<Command>>> commandQueue() {
        Serde<Command> innerSerde = new KafkaProtobufSerde<>(schemaRegistryClient, Command.class);
        innerSerde.configure(streamsConfig.originals(), false);
        return Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore(COMMAND_QUEUE),
                        Serdes.UUID(),
                        Serdes.ListSerde(ArrayList.class, innerSerde));
    }

    @SuppressWarnings("unchecked") // ArrayList has no inner type that can be anticipated
    private StoreBuilder<KeyValueStore<UUID, List<UUID>>> semaphoreStore() {
        return Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore(SEMAPHORE_STORE),
                        Serdes.UUID(),
                        Serdes.ListSerde(ArrayList.class, Serdes.UUID()));
    }
}
