package com.predman.content.dto.project_statistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ProjectStatisticsDto(
    UUID id,

    @JsonProperty(value = "project_id")
    @NonNull
    UUID projectId,
    
    @JsonProperty(value = "team_size")
    Integer teamSize,
    
    @JsonProperty(value = "days_since_start")
    Integer daysSinceStart,

    @JsonProperty(value = "remaining_tasks")
    Integer remainingTasks,

    @JsonProperty(value = "remaining_story_points")
    Double remainingStoryPoints,

    @JsonProperty(value = "dependency_coefficient")
    Double dependencyCoefficient,

    @JsonProperty(value = "critical_path_length")
    Double criticalPathLength,

    @JsonProperty(value = "sum_experience")
    Double sumExperience,

    @JsonProperty(value = "available_hours")
    Double availableHours,

    @JsonProperty(value = "external_risk_probability")
    Double externalRiskProbability,

    @JsonProperty(value = "saved_at")
    LocalDateTime savedAt
) {
}
