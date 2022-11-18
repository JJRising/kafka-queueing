package com.nuvalence.kafka.queueing.kstream.validation;

import com.nuvalence.kafka.queueing.proto.Event;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import java.util.UUID;

public class ValidationProcessor implements Processor<UUID, Event, UUID, Event> {

    private ProcessorContext<UUID, Event> context;

    @Override
    public void init(ProcessorContext<UUID, Event> context) {
        this.context = context;
    }

    @Override
    public void process(Record<UUID, Event> record) {
        context.forward(record);
    }
}
