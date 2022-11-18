package com.nuvalence.kafka.queueing.kstream.semaphore;

import com.nuvalence.kafka.queueing.kstream.config.QueueingConfig;
import com.nuvalence.kafka.queueing.proto.Event;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.List;
import java.util.UUID;

import static com.nuvalence.kafka.queueing.kstream.semaphore.SemaphoreProcessorSupplier.SEMAPHORE_STORE;
import static com.nuvalence.kafka.queueing.kstream.utils.UUIDUtils.uuidFromBytes;

public class SemaphoreProcessor implements Processor<UUID, Event, UUID, Event> {

    private final QueueingConfig queueingConfig;

    private final Semaphore semaphore;

    private KeyValueStore<UUID, List<UUID>> semaphoreStore;

    public SemaphoreProcessor(QueueingConfig queueingConfig, Semaphore semaphore) {
        this.queueingConfig = queueingConfig;
        this.semaphore = semaphore;
    }

    @Override
    public void init(ProcessorContext<UUID, Event> context) {
        this.semaphoreStore = context.getStateStore(SEMAPHORE_STORE);
    }

    @Override
    public void process(Record<UUID, Event> record) {
        UUID resourceId = record.key();
        Event event = record.value();
        if (event.getType() == Event.TYPE.TERMINATING) {
            int remainingAllocatable = removeCommandIdFromSemaphore(resourceId, uuidFromBytes(event.getId()));
            if (remainingAllocatable == 1) {
                raiseSemaphoreReleaseFlag(resourceId);
            }
        }
    }

    /**
     * Remove a command from the semaphore list indicating it is no longer holding a lock on the resource
     * @param resourceId of the resource in question
     * @param commandId to remove from the list of inflight
     * @return number of commands that can still be sent to the resource
     */
    private int removeCommandIdFromSemaphore(UUID resourceId, UUID commandId) {
        List<UUID> resourceSemaphoreList = semaphoreStore.get(resourceId);
        resourceSemaphoreList.remove(commandId);
        semaphoreStore.put(resourceId, resourceSemaphoreList);
        return queueingConfig.getDefaultMaxInflight() - resourceSemaphoreList.size();
    }

    /**
     * Indicate that a resource no longer has zero available semaphore locks
     * @param resourceId in question
     */
    private void raiseSemaphoreReleaseFlag(UUID resourceId) {
        semaphore.releaseResource(resourceId);
    }
}
