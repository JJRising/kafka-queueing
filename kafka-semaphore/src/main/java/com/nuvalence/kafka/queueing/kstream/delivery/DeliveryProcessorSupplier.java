package com.nuvalence.kafka.queueing.kstream.delivery;

import com.nuvalence.kafka.queueing.proto.Event;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DeliveryProcessorSupplier implements ProcessorSupplier<UUID, Event, UUID, Event> {

    @Override
    public Processor<UUID, Event, UUID, Event> get() {
        return new DeliveryProcessor();
    }
}
