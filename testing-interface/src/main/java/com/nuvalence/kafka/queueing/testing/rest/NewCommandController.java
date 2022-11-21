package com.nuvalence.kafka.queueing.testing.rest;

import com.nuvalence.kafka.queueing.testing.dto.NewCommandResponse;
import com.nuvalence.kafka.queueing.testing.kafka.KafkaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/command")
public class NewCommandController {

    private final KafkaService kafkaService;

    public NewCommandController(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @PostMapping
    public ResponseEntity<NewCommandResponse> createNewCommand() {
        UUID resourceId = UUID.randomUUID();
        UUID commandId = UUID.randomUUID();
        kafkaService.sendNewCommandRequest(resourceId, commandId);
        return new ResponseEntity<>(new NewCommandResponse(resourceId, commandId), HttpStatus.CREATED);
    }

    @PostMapping("/{resourceId}")
    public ResponseEntity<NewCommandResponse> createNewCommand(@PathVariable UUID resourceId) {
        UUID commandId = UUID.randomUUID();
        kafkaService.sendNewCommandRequest(resourceId, commandId);
        return new ResponseEntity<>(new NewCommandResponse(resourceId, commandId), HttpStatus.CREATED);
    }
}
