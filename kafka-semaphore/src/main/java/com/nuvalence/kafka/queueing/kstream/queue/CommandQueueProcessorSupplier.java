package com.nuvalence.kafka.queueing.kstream.queue;

import com.nuvalence.kafka.queueing.proto.Command;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CommandQueueProcessorSupplier implements ProcessorSupplier<UUID, Command, UUID, Command> {

    private final QueueingConfig queueingConfig;

    public CommandQueueProcessorSupplier(QueueingConfig queueingConfig) {
        this.queueingConfig = queueingConfig;
    }

    @Override
    public Processor<UUID, Command, UUID, Command> get() {
        return new CommandQueueProcessor(queueingConfig);
    }
}
