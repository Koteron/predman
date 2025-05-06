package com.predman.content.dto.task_dependency;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.springframework.lang.NonNull;

import java.util.UUID;

@Builder
public record TaskDependencyDto(
        @NonNull
        @JsonProperty(value = "task_id")
        UUID taskId,

        @NonNull
        @JsonProperty(value = "dependency_id")
        UUID dependencyId
) {
}
