package com.nuvalence.kafka.queueing.kstream.queue;

import com.nuvalence.kafka.queueing.Command;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CommandQueueProcessorSupplier implements ProcessorSupplier<UUID, Command, UUID, Command> {

    @Override
    public Processor<UUID, Command, UUID, Command> get() {
        return new CommandQueueProcessor();
    }
}
