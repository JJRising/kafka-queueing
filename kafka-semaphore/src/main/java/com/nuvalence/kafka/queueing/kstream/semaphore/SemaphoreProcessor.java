package com.nuvalence.kafka.queueing.kstream.semaphore;

import com.nuvalence.kafka.queueing.proto.Event;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import java.util.UUID;

import static com.nuvalence.kafka.queueing.kstream.semaphore.SemaphoreProcessorSupplier.SEMAPHORE_RELEASE_MAP;
import static com.nuvalence.kafka.queueing.kstream.semaphore.SemaphoreProcessorSupplier.SEMAPHORE_STORE;
import static com.nuvalence.kafka.queueing.kstream.utils.UUIDUtils.uuidFromBytes;

public class SemaphoreProcessor implements Processor<UUID, Event, UUID, Event> {

    private final Semaphore semaphore;

    public SemaphoreProcessor(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    @Override
    public void init(ProcessorContext<UUID, Event> context) {
        this.semaphore.init(
                context.getStateStore(SEMAPHORE_STORE),
                context.getStateStore(SEMAPHORE_RELEASE_MAP));
    }

    @Override
    public void process(Record<UUID, Event> record) {
        Event event = record.value();
        if (event.getType() == Event.TYPE.TERMINATING) {
            semaphore.releaseSemaphore(record.key(), uuidFromBytes(event.getId()));
        }
    }
}
