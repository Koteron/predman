package com.predman.content.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.predman.content.dto.task.TaskDto;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ProjectTaskListDto(

        UUID id,

        String name,

        String description,

        @JsonProperty(value = "owner_id")
        UUID ownerId,

        List<TaskDto> tasks
) { }
