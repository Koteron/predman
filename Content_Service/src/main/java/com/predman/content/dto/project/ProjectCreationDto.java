package com.predman.content.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ProjectCreationDto(

        @NotNull
        String name,

        String description,

        @NotNull
        @JsonProperty(value = "due_date")
        LocalDate dueDate

) { }
