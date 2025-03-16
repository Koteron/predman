package com.predman.content.dto.project_statistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ProjectStatisticsDto(
    UUID id,

    @JsonProperty(value = "project_id")
    @NonNull
    UUID projectId,
    
    @JsonProperty(value = "easy_tasks")
    Integer easyTasks,
    
    @JsonProperty(value = "medium_tasks")
    Integer mediumTasks,
    
    @JsonProperty(value = "hard_tasks")
    Integer hardTasks,
    
    @JsonProperty(value = "completed_easy_tasks")
    Integer completedEasyTasks,
    
    @JsonProperty(value = "completed_medium_tasks")
    Integer completedMediumTasks,
    
    @JsonProperty(value = "completed_hard_tasks")
    Integer completedHardTasks,
    
    @JsonProperty(value = "team_size")
    Integer teamSize,
    
    @JsonProperty(value = "days_since_start")
    Integer daysSinceStart,
    
    @JsonProperty(value = "days_to_deadline")
    Integer daysToDeadline,
    
    @JsonProperty(value = "completion_probability")
    Double completionProbability,
    
    @JsonProperty(value = "estimated_date")
    LocalDate estimatedDate,

    @JsonProperty(value = "saved_at")
    LocalDateTime savedAt
) {
}
