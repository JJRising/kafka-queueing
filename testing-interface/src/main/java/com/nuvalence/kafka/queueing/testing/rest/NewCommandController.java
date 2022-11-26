package com.nuvalence.kafka.queueing.testing.rest;

import com.nuvalence.kafka.queueing.testing.dto.NewCommandResponse;
import com.nuvalence.kafka.queueing.testing.kafka.KafkaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/command")
public class NewCommandController {

    private final KafkaService kafkaService;

    public NewCommandController(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @PostMapping
    public ResponseEntity<NewCommandResponse> createNewCommand(@RequestBody String message) {
        UUID resourceId = UUID.randomUUID();
        UUID commandId = UUID.randomUUID();
        kafkaService.sendNewCommandRequest(resourceId, commandId, message);
        return new ResponseEntity<>(new NewCommandResponse(resourceId, commandId), HttpStatus.CREATED);
    }

    @PostMapping("/{resourceId}")
    public ResponseEntity<NewCommandResponse> createNewCommand(
            @PathVariable UUID resourceId, @RequestBody String message) {
        UUID commandId = UUID.randomUUID();
        kafkaService.sendNewCommandRequest(resourceId, commandId, message);
        return new ResponseEntity<>(new NewCommandResponse(resourceId, commandId), HttpStatus.CREATED);
    }
}
