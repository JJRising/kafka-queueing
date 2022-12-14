package com.nuvalence.kafka.queueing.kstream.delivery;

import com.nuvalence.kafka.queueing.proto.Command;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import java.util.UUID;

/**
 * This class is where you would do your pre-delivery processing for commands just before they are
 * delivered, and after they have obtained a semaphore.
 */
public class DeliveryProcessor implements Processor<UUID, Command, UUID, Command> {

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
