package com.predman.content.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ProjectCreationDto(

        @NotNull
        String name,

        String description,

        @NotNull
        @JsonProperty(value = "due_date")
        LocalDate dueDate

) { }
