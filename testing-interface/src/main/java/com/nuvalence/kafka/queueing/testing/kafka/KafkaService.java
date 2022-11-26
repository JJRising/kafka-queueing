package com.nuvalence.kafka.queueing.testing.kafka;

import com.nuvalence.kafka.queueing.proto.Command;
import com.nuvalence.kafka.queueing.proto.Event;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.nuvalence.kafka.queueing.testing.utils.UUIDUtils.bytesFromUUID;

@Service
public class KafkaService {

    private final KafkaTemplate<UUID, Command> commandTemplate;
    private final KafkaTemplate<UUID, Event> eventTemplate;

    private final KafkaConfig config;

    public KafkaService(KafkaTemplate<UUID, Command> commandTemplate,
                        KafkaTemplate<UUID, Event> eventTemplate,
                        KafkaConfig config) {
        this.commandTemplate = commandTemplate;
        this.eventTemplate = eventTemplate;
        this.config = config;
    }

    public void sendNewCommandRequest(UUID resourceId, UUID commandId, String message) {
        commandTemplate.send(
                config.getCommandRequestTopic(),
                resourceId,
                Command.newBuilder()
                        .setId(bytesFromUUID(commandId))
                        .setMessage(message)
                        .build());
    }

    public void sendTerminatingEvent(UUID resourceId, UUID commandId) {
        eventTemplate.send(
                config.getEventTopic(),
                resourceId,
                Event.newBuilder()
                        .setId(bytesFromUUID(commandId))
                        .setType(Event.TYPE.TERMINATING)
                        .setMessage("Command completed on " + resourceId)
                        .build());
    }
}
