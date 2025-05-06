package com.predman.content.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record ProjectUpdateDto(
        String name,

        String description,

        @JsonProperty(value = "due_date")
        LocalDate dueDate,

        @JsonProperty(value = "available_hours")
        Double availableHours,

        @JsonProperty(value = "sum_experience")
        Double sumExperience,

        @JsonProperty(value = "external_risk_probability")
        Double externalRiskProbability

) { }
