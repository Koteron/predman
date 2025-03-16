package com.predman.content.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.predman.content.common.Complexity;
import lombok.Builder;
import org.springframework.lang.NonNull;

import java.util.UUID;

@Builder
public record TaskCreationDto (
    @JsonProperty("project_id")
    @NonNull
    UUID projectId,

    @NonNull
    String name,

    String description,

    @NonNull
    Complexity complexity
) { }
