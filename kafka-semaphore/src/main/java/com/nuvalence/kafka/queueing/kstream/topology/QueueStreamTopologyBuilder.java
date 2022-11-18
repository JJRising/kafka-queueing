package com.nuvalence.kafka.queueing.kstream.topology;

import com.nuvalence.kafka.queueing.Command;
import com.nuvalence.kafka.queueing.kstream.config.TopologyConfig;
import com.nuvalence.kafka.queueing.kstream.queue.CommandQueueProcessorSupplier;
import com.nuvalence.kafka.queueing.kstream.semaphore.SemaphoreProcessorSupplier;
import com.nuvalence.kafka.queueing.proto.Event;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.springframework.stereotype.Component;

@Component
public class QueueStreamTopologyBuilder {

    public static final String COMMAND_SOURCE = "command-source";
    public static final String COMMAND_QUEUE = "command-queue";
    public static final String COMMAND_SINK = "command-sink";
    public static final String SEMAPHORE_RELEASE_SOURCE = "semaphore-release-source";
    public static final String SEMAPHORE_RELEASE_PROCESSOR = "semaphore-release-processor";

    private final TopologyConfig topologyConfig;
    private final StreamsConfig streamsConfig;
    private final SchemaRegistryClient schemaRegistryClient;

    private final CommandQueueProcessorSupplier commandQueueProcessorSupplier;
    private final SemaphoreProcessorSupplier semaphoreProcessorSupplier;

    public QueueStreamTopologyBuilder(TopologyConfig topologyConfig,
                                      StreamsConfig streamsConfig,
                                      SchemaRegistryClient schemaRegistryClient,
                                      CommandQueueProcessorSupplier commandQueueProcessorSupplier,
                                      SemaphoreProcessorSupplier semaphoreProcessorSupplier) {
        this.topologyConfig = topologyConfig;
        this.streamsConfig = streamsConfig;
        this.schemaRegistryClient = schemaRegistryClient;
        this.commandQueueProcessorSupplier = commandQueueProcessorSupplier;
        this.semaphoreProcessorSupplier = semaphoreProcessorSupplier;
    }

    public Topology constructQueueTopology() {
        return new Topology()
                .addSource(COMMAND_SOURCE,
                        new UUIDDeserializer(),
                        new KafkaProtobufDeserializer<>(schemaRegistryClient, streamsConfig.originals(), Command.class),
                        topologyConfig.getInputTopicName())
                .addProcessor(COMMAND_QUEUE, commandQueueProcessorSupplier, COMMAND_SOURCE)
                .addSink(COMMAND_SINK,
                        topologyConfig.getOutputTopicName(),
                        new UUIDSerializer(),
                        new KafkaProtobufSerializer<Command>(schemaRegistryClient, streamsConfig.originals()),
                        COMMAND_QUEUE)
                .addSource(SEMAPHORE_RELEASE_SOURCE,
                        new UUIDDeserializer(),
                        new KafkaProtobufDeserializer<>(schemaRegistryClient, streamsConfig.originals(), Event.class))
                .addProcessor(SEMAPHORE_RELEASE_PROCESSOR, semaphoreProcessorSupplier, SEMAPHORE_RELEASE_SOURCE);
    }
}
