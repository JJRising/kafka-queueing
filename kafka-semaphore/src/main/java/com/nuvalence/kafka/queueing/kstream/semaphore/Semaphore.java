package com.nuvalence.kafka.queueing.kstream.semaphore;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class Semaphore {
    private final Map<UUID, Boolean> semaphoreReleaseFlag = new HashMap<>();

    public List<UUID> getReleasedResources() {
        return semaphoreReleaseFlag.entrySet().stream()
                .filter(Map.Entry::getValue)
                .peek(e -> e.setValue(false))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void releaseResource(UUID resourceId) {
        semaphoreReleaseFlag.put(resourceId, true);
    }
}
