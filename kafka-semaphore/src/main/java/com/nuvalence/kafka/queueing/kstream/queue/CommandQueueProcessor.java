package com.nuvalence.kafka.queueing.kstream.queue;

import com.nuvalence.kafka.queueing.Command;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import java.util.UUID;

public class CommandQueueProcessor implements Processor<UUID, Command, UUID, Command> {

    private ProcessorContext<UUID, Command> context;

    @Override
    public void init(ProcessorContext<UUID, Command> context) {
        this.context = context;

        // TODO - implement a scheduled process to check for semaphore releases
    }

    @Override
    public void process(Record<UUID, Command> record) {
        // TODO - implement queueing
        this.context.forward(record);
    }
}
