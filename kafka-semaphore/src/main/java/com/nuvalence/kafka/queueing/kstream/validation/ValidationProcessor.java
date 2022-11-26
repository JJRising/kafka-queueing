package com.nuvalence.kafka.queueing.kstream.validation;

import com.nuvalence.kafka.queueing.proto.Command;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import java.util.UUID;

/**
 * This class would be where you would include any upstream processing and validation of commands
 * before they attempt to obtain a semaphore or be queued.
 */
public class ValidationProcessor implements Processor<UUID, Command, UUID, Command> {

    private ProcessorContext<UUID, Command> context;

    @Override
    public void init(ProcessorContext<UUID, Command> context) {
        this.context = context;
    }

    @Override
    public void process(Record<UUID, Command> record) {
        context.forward(record);
    }
}
