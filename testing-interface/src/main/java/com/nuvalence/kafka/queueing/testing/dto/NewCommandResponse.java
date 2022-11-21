package com.nuvalence.kafka.queueing.testing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class NewCommandResponse {
    UUID resourceId;
    UUID commandId;
}
