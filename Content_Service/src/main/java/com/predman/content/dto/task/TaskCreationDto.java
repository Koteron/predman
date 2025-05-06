package com.predman.content.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("story_points")
    Double storyPoints
) { }
