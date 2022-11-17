package com.nuvalence.kafka.queueing.kstream;

import com.nuvalence.kafka.queueing.kstream.config.TopologyConfig;
import com.nuvalence.kafka.queueing.kstream.topology.QueueStreamTopologyBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
@Slf4j
public class StreamManager {

    private final QueueStreamTopologyBuilder topologyBuilder;
    private final TopologyConfig topologyConfig;

    private KafkaStreams streams;

    public StreamManager(QueueStreamTopologyBuilder topologyBuilder,
                         TopologyConfig topologyConfig) {
        this.topologyBuilder = topologyBuilder;
        this.topologyConfig = topologyConfig;
    }

    @PostConstruct
    public void init() {
        Topology topology = topologyBuilder.constructQueueTopology();
        streams = new KafkaStreams(topology, topologyConfig.streamsConfig());
        streams.setUncaughtExceptionHandler(uncaughtExceptionHandler());
        streams.start();
        log.info("Started Queue Stream");
    }

    @PreDestroy
    public void onExit() {
        log.error("Received shutdown hook. Attempting to gracefully close Queue Stream.");
        streams.close();
    }

    private StreamsUncaughtExceptionHandler uncaughtExceptionHandler() {
        return (Throwable throwable) -> {
            log.error(
                    String.format("An exception was thrown within the Streams thread. " +
                                    "Performing configured behaviour: %s",
                            topologyConfig.getStreamThreadExceptionResponse().name),
                    throwable);
            return topologyConfig.getStreamThreadExceptionResponse();
        };
    }
}
