package com.nuvalence.kafka.queueing.kstream.semaphore;

import com.nuvalence.kafka.queueing.proto.Event;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SemaphoreProcessorSupplier implements ProcessorSupplier<UUID, Event, UUID, Event> {

    public static final String SEMAPHORE_STORE = "semaphore-store";
    public static final String SEMAPHORE_RELEASE_MAP = "semaphore-release-map";

    private final SemaphoreConfig semaphoreConfig;

    public SemaphoreProcessorSupplier(SemaphoreConfig semaphoreConfig) {
        this.semaphoreConfig = semaphoreConfig;
    }

    @Override
    public Processor<UUID, Event, UUID, Event> get() {
        return new SemaphoreProcessor(new Semaphore(semaphoreConfig.getSemaphoreLimits()));
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        @SuppressWarnings("unchecked") // ArrayList has no inner type that can be anticipated
        StoreBuilder<KeyValueStore<UUID, List<UUID>>> keyValueStoreStoreBuilder =
                Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore(SEMAPHORE_STORE),
                        Serdes.UUID(),
                        Serdes.ListSerde(ArrayList.class, Serdes.UUID()));
        return Collections.singleton(keyValueStoreStoreBuilder);
    }
}
