package com.nuvalence.kafka.queueing.kstream.queue;

import com.nuvalence.kafka.queueing.proto.Command;
import com.nuvalence.kafka.queueing.proto.Event;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.nuvalence.kafka.queueing.kstream.topology.QueueStreamTopologyBuilder.COMMAND_QUEUE;
import static com.nuvalence.kafka.queueing.kstream.topology.QueueStreamTopologyBuilder.SEMAPHORE_STORE;
import static com.nuvalence.kafka.queueing.kstream.utils.UUIDUtils.uuidFromBytes;

/**
 * This processor takes in events from the protected resource, and if they are a terminating events,
 * it will release the semaphore, and forward the next command in the queue (if one exists).
 */
public class SemaphoreProcessor implements Processor<UUID, Event, UUID, Command> {

    private ProcessorContext<UUID, Command> context;
    private KeyValueStore<UUID, List<Command>> commandQueue;
    private KeyValueStore<UUID, List<UUID>> semaphoreStore;

    private final QueueingConfig config;

    public SemaphoreProcessor(QueueingConfig config) {
        this.config = config;
    }

    @Override
    public void init(ProcessorContext<UUID, Command> context) {
        this.context = context;
        this.commandQueue = context.getStateStore(COMMAND_QUEUE);
        this.semaphoreStore = context.getStateStore(SEMAPHORE_STORE);
    }

    @Override
    public void process(Record<UUID, Event> record) {
        Event event = record.value();
        if (event.getType() == Event.TYPE.TERMINATING) {
            if (releaseSemaphore(record.key(), uuidFromBytes(event.getId()))) {
                forwardNextCommand(record.key());
            }
        }
    }

    private boolean releaseSemaphore(UUID resourceId, UUID terminatingCommandId) {
        List<UUID> activeSemaphores = Optional.ofNullable(semaphoreStore.get(resourceId)).orElse(new ArrayList<>());
        if (activeSemaphores.remove(terminatingCommandId)) {
            semaphoreStore.put(resourceId, activeSemaphores);
            int semaphoreLimit = config.getSemaphoreLimits().getOrDefault(resourceId, config.getDefaultSemaphoreLimit());
            return semaphoreLimit - activeSemaphores.size() > 0;
        } else {
            return false;
        }
    }

    private void forwardNextCommand(UUID resourceId) {
        List<Command> queue = commandQueue.get(resourceId);
        if (queue != null && queue.size() > 0) {
            // pop the next command from the state store
            Command next = queue.remove(0);
            commandQueue.put(resourceId, queue);

            // put it in the semaphore list
            List<UUID> activeSemaphores = Optional.ofNullable(semaphoreStore.get(resourceId)).orElse(new ArrayList<>());
            activeSemaphores.add(uuidFromBytes(next.getId()));
            semaphoreStore.put(resourceId, activeSemaphores);

            context.forward(new Record<>(resourceId, next, Instant.now().toEpochMilli()));
        }
    }
}
