package com.predman.content.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.predman.content.common.TaskStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record TaskDto (
    UUID id,

    @JsonProperty("project_id")
    UUID projectId,

    String name,

    String description,

    @JsonProperty("story_points")
    Double storyPoints,

    TaskStatus status
) { }
