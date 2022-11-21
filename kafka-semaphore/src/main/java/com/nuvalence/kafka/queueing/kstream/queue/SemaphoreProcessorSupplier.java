package com.nuvalence.kafka.queueing.kstream.queue;

import com.nuvalence.kafka.queueing.proto.Command;
import com.nuvalence.kafka.queueing.proto.Event;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SemaphoreProcessorSupplier implements ProcessorSupplier<UUID, Event, UUID, Command> {

    private final QueueingConfig queueingConfig;

    public SemaphoreProcessorSupplier(QueueingConfig queueingConfig) {
        this.queueingConfig = queueingConfig;
    }

    @Override
    public Processor<UUID, Event, UUID, Command> get() {
        return new SemaphoreProcessor(queueingConfig);
    }
}
