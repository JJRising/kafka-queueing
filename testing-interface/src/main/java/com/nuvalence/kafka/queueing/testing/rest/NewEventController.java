package com.nuvalence.kafka.queueing.testing.rest;

import com.nuvalence.kafka.queueing.testing.kafka.KafkaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/event")
public class NewEventController {

    private final KafkaService kafkaService;

    public NewEventController(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @PostMapping("/{resourceId}/{commandId}")
    public ResponseEntity<Void> createNewEvent(@PathVariable UUID resourceId, @PathVariable UUID commandId) {
        kafkaService.sendTerminatingEvent(resourceId, commandId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
