package com.predman.content.dto.task;

import com.predman.content.common.Complexity;
import com.predman.content.common.TaskStatus;
import lombok.Builder;


@Builder
public record TaskUpdateDto (

        String name,

        String description,

        Complexity complexity,

        TaskStatus status
) { }
