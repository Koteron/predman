package com.predman.content.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.predman.content.common.TaskStatus;
import lombok.Builder;


@Builder
public record TaskUpdateDto (

        String name,

        String description,

        @JsonProperty("story_points")
        Double storyPoints,

        TaskStatus status
) { }
