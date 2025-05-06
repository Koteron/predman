package com.predman.content.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record ProjectFullInfoDto(

    UUID id,

    String name,

    String description,

    @JsonProperty(value = "due_date")
    LocalDate dueDate,

    @JsonProperty(value = "certainty_percent")
    Double certaintyPercent,

    @JsonProperty(value = "predicted_deadline")
    LocalDate predictedDeadline,

    @JsonProperty(value = "available_hours")
    Double availableHours,

    @JsonProperty(value = "sum_experience")
    Double sumExperience,

    @JsonProperty(value = "external_risk_probability")
    Double externalRiskProbability,

    @JsonProperty(value = "owner_id")
    UUID ownerId
) { }
