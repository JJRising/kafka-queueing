package com.nuvalence.kafka.queueing.kstream.queue;

import com.nuvalence.kafka.queueing.proto.Command;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.*;

import static com.nuvalence.kafka.queueing.kstream.topology.QueueStreamTopologyBuilder.COMMAND_QUEUE;
import static com.nuvalence.kafka.queueing.kstream.topology.QueueStreamTopologyBuilder.SEMAPHORE_STORE;
import static com.nuvalence.kafka.queueing.kstream.utils.UUIDUtils.uuidFromBytes;

public class CommandQueueProcessor implements Processor<UUID, Command, UUID, Command> {

    private ProcessorContext<UUID, Command> context;
    private KeyValueStore<UUID, List<Command>> commandQueue;
    private KeyValueStore<UUID, List<UUID>> semaphoreStore;

    private final int defaultLimit;
    private final Map<UUID, Integer> semaphoreLimits;

    public CommandQueueProcessor(QueueingConfig queueingConfig) {
        this.defaultLimit = queueingConfig.getDefaultSemaphoreLimit();
        this.semaphoreLimits = queueingConfig.getSemaphoreLimits();
    }

    @Override
    public void init(ProcessorContext<UUID, Command> context) {
        this.context = context;
        this.commandQueue = context.getStateStore(COMMAND_QUEUE);
        this.semaphoreStore = context.getStateStore(SEMAPHORE_STORE);
    }

    @Override
    public void process(Record<UUID, Command> record) {
        UUID resourceId = record.key();
        UUID commandId = uuidFromBytes(record.value().getId());

        if (acquireSemaphore(resourceId, commandId)) {
            context.forward(record);
        } else {
            enqueueCommand(resourceId, record.value());
        }
    }

    private boolean acquireSemaphore(UUID resourceId, UUID commandId) {
        List<UUID> activeSemaphores = Optional.ofNullable(semaphoreStore.get(resourceId)).orElse(new ArrayList<>());
        if (activeSemaphores.size() < semaphoreLimits.getOrDefault(resourceId, defaultLimit)) {
            activeSemaphores.add(commandId);
            semaphoreStore.put(resourceId, activeSemaphores);
            return true;
        } else {
            return false;
        }
    }

    private void enqueueCommand(UUID resourceId, Command command) {
        List<Command> queue = Optional.ofNullable(commandQueue.get(resourceId)).orElse(new ArrayList<>());
        queue.add(command);
        commandQueue.put(resourceId, queue);
    }
}
