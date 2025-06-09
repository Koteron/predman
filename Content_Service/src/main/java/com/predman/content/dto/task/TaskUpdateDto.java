package com.predman.content.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.predman.content.common.TaskStatus;
import lombok.Builder;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;


@Builder
public record TaskUpdateDto (

        String name,

        String description,

        @JsonProperty("story_points")
        Double storyPoints,

        UUID next,

        @NonNull
        Boolean isNextUpdated,

        TaskStatus status
) { }
