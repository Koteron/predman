package com.predman.content.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;

@Builder
public record SortedTasksDto (

        List<TaskDto> planned,

        List<TaskDto> inprogress,

        List<TaskDto> completed
) { }
