package com.nuvalence.kafka.queueing.kstream.topology;

import com.nuvalence.kafka.queueing.Command;
import com.nuvalence.kafka.queueing.kstream.config.TopologyConfig;
import com.nuvalence.kafka.queueing.kstream.processor.CommandQueueProcessorSupplier;
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

    private final TopologyConfig topologyConfig;
    private final StreamsConfig streamsConfig;
    private final SchemaRegistryClient schemaRegistryClient;

    private final CommandQueueProcessorSupplier commandQueueProcessorSupplier;

    public QueueStreamTopologyBuilder(TopologyConfig topologyConfig,
                                      StreamsConfig streamsConfig,
                                      SchemaRegistryClient schemaRegistryClient,
                                      CommandQueueProcessorSupplier commandQueueProcessorSupplier) {
        this.topologyConfig = topologyConfig;
        this.streamsConfig = streamsConfig;
        this.schemaRegistryClient = schemaRegistryClient;
        this.commandQueueProcessorSupplier = commandQueueProcessorSupplier;
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
                        COMMAND_QUEUE);
    }
}
