package com.nuvalence.kafka.queueing.kstream.semaphore;

import org.apache.kafka.streams.state.KeyValueStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Semaphore {
    private final Map<UUID, Integer> semaphoreLimits;

    private KeyValueStore<UUID, List<UUID>> semaphoreStore;
    private KeyValueStore<UUID, Short> semaphoreReleaseMap;

    public Semaphore(Map<UUID, Integer> semaphoreLimits) {
        this.semaphoreLimits = semaphoreLimits;
    }

    public void init(KeyValueStore<UUID, List<UUID>> semaphoreStore,
                     KeyValueStore<UUID, Short> semaphoreReleaseMap) {
        this.semaphoreStore = semaphoreStore;
        this.semaphoreReleaseMap = semaphoreReleaseMap;
    }

    public boolean acquireSemaphore(UUID resourceId, UUID commandId) {
        List<UUID> activeSemaphores = semaphoreStore.get(resourceId);
        if (activeSemaphores.size() < semaphoreLimits.get(resourceId)) {
            activeSemaphores.add(commandId);
            semaphoreStore.put(resourceId, activeSemaphores);
            return true;
        } else {
            return false;
        }
    }

    public void releaseSemaphore(UUID resourceId, UUID commandId) {
        List<UUID> activeSemaphores = semaphoreStore.get(resourceId);
        if (activeSemaphores.remove(commandId)) {
            semaphoreStore.put(resourceId, activeSemaphores);
            if (semaphoreLimits.get(resourceId) - activeSemaphores.size() == 1) {
                semaphoreReleaseMap.put(resourceId, (short) 1);
            }
        }
    }

    public List<UUID> getReleasedResources() {
        List<UUID> ret = new ArrayList<>();
        semaphoreReleaseMap.all().forEachRemaining(kv -> {
            if (kv.value > 0) {
                ret.add(kv.key);
                semaphoreReleaseMap.put(kv.key, (short) 0);
            }
        });
        return ret;
    }
}
