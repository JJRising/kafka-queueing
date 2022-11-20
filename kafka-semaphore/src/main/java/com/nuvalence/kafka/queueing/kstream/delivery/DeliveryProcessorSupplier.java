package com.nuvalence.kafka.queueing.kstream.delivery;

import com.nuvalence.kafka.queueing.proto.Command;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DeliveryProcessorSupplier implements ProcessorSupplier<UUID, Command, UUID, Command> {

    @Override
    public Processor<UUID, Command, UUID, Command> get() {
        return new DeliveryProcessor();
    }
}
