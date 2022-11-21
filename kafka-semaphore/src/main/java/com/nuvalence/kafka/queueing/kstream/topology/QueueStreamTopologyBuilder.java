package com.nuvalence.kafka.queueing.kstream.topology;

import com.nuvalence.kafka.queueing.kstream.config.TopologyConfig;
import com.nuvalence.kafka.queueing.kstream.delivery.DeliveryProcessorSupplier;
import com.nuvalence.kafka.queueing.kstream.queue.CommandQueueProcessorSupplier;
import com.nuvalence.kafka.queueing.kstream.semaphore.SemaphoreProcessorSupplier;
import com.nuvalence.kafka.queueing.kstream.validation.ValidationProcessorSupplier;
import com.nuvalence.kafka.queueing.proto.Command;
import com.nuvalence.kafka.queueing.proto.Event;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.springframework.stereotype.Component;

import static com.nuvalence.kafka.queueing.kstream.semaphore.SemaphoreProcessorSupplier.SEMAPHORE_RELEASE_MAP;
import static com.nuvalence.kafka.queueing.kstream.semaphore.SemaphoreProcessorSupplier.SEMAPHORE_STORE;

@Component
public class QueueStreamTopologyBuilder {

    public static final String COMMAND_SOURCE = "command-source";
    public static final String COMMAND_VALIDATION = "command-validation";
    public static final String COMMAND_QUEUE = "command-queue";
    public static final String COMMAND_DELIVERY = "command-delivery";
    public static final String COMMAND_SINK = "command-sink";
    public static final String SEMAPHORE_RELEASE_SOURCE = "semaphore-release-source";
    public static final String SEMAPHORE_RELEASE_PROCESSOR = "semaphore-release-processor";

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
                .addProcessor(COMMAND_VALIDATION, commandValidationProcessorSupplier, COMMAND_SOURCE)
                .addProcessor(COMMAND_QUEUE, commandQueueProcessorSupplier, COMMAND_VALIDATION)
                .addProcessor(COMMAND_DELIVERY, commandDeliveryProcessorSupplier, COMMAND_QUEUE)
                .addSink(COMMAND_SINK,
                        topologyConfig.getOutputTopicName(),
                        new UUIDSerializer(),
                        new KafkaProtobufSerializer<Command>(schemaRegistryClient, streamsConfig.originals()),
                        COMMAND_DELIVERY)
                .addSource(SEMAPHORE_RELEASE_SOURCE,
                        new UUIDDeserializer(),
                        new KafkaProtobufDeserializer<>(schemaRegistryClient, streamsConfig.originals(), Event.class),
                        topologyConfig.getEventTopicName())
                .addProcessor(SEMAPHORE_RELEASE_PROCESSOR, semaphoreProcessorSupplier, SEMAPHORE_RELEASE_SOURCE)
                .connectProcessorAndStateStores(COMMAND_QUEUE, SEMAPHORE_STORE, SEMAPHORE_RELEASE_MAP);
    }
}
