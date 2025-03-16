package com.predman.content.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.util.UUID;

public record TaskProjectPairDto(
    @NonNull
    @JsonProperty(value = "task_id")
    UUID taskId,

    @NonNull
    @JsonProperty(value = "project_id")
    UUID projectId
) { }
