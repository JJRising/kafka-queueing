package com.nuvalence.kafka.queueing.kstream.queue;

import com.nuvalence.kafka.queueing.kstream.semaphore.Semaphore;
import com.nuvalence.kafka.queueing.proto.Command;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.nuvalence.kafka.queueing.kstream.queue.CommandQueueProcessorSupplier.COMMAND_QUEUE;
import static com.nuvalence.kafka.queueing.kstream.semaphore.SemaphoreProcessorSupplier.SEMAPHORE_RELEASE_MAP;
import static com.nuvalence.kafka.queueing.kstream.semaphore.SemaphoreProcessorSupplier.SEMAPHORE_STORE;
import static com.nuvalence.kafka.queueing.kstream.utils.UUIDUtils.uuidFromBytes;

public class CommandQueueProcessor implements Processor<UUID, Command, UUID, Command> {

    private ProcessorContext<UUID, Command> context;
    private KeyValueStore<UUID, List<Command>> commandQueue;

    private final Semaphore semaphore;

    private final QueueingConfig queueingConfig;

    public CommandQueueProcessor(Semaphore semaphore,
                                 QueueingConfig queueingConfig) {
        this.semaphore = semaphore;
        this.queueingConfig = queueingConfig;
    }

    @Override
    public void init(ProcessorContext<UUID, Command> context) {
        this.context = context;
        this.commandQueue = context.getStateStore(COMMAND_QUEUE);
        this.semaphore.init(
                context.getStateStore(SEMAPHORE_STORE),
                context.getStateStore(SEMAPHORE_RELEASE_MAP));

        this.context.schedule(Duration.ofMillis(queueingConfig.getSemaphoreScanIntervalMillis()),
                PunctuationType.WALL_CLOCK_TIME,
                timestamp -> semaphore.getReleasedResources().forEach(uuid -> {
                    List<Command> queue = commandQueue.get(uuid);
                    if (queue.size() > 0) {
                        Command first = queue.remove(0);
                        commandQueue.put(uuid, queue);
                        context.forward(new Record<>(uuid, first, timestamp));
                    }
                }));
    }

    @Override
    public void process(Record<UUID, Command> record) {
        UUID resourceId = record.key();
        UUID commandId = uuidFromBytes(record.value().getId());

        if (semaphore.acquireSemaphore(resourceId, commandId)) {
            context.forward(record);
        } else {
            List<Command> queue = Optional.ofNullable(commandQueue.get(resourceId)).orElse(new ArrayList<>());
            queue.add(record.value());
            commandQueue.put(resourceId, queue);
        }
    }
}
