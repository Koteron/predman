package com.predman.content.dto.task_assignment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.UUID;

@Builder
public record TaskAssignmentDto(
        @JsonProperty(value = "user_id")
        UUID userId,

        @JsonProperty(value = "task_id")
        UUID taskId
)
{}
