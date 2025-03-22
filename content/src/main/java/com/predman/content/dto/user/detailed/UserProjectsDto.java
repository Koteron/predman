package com.predman.content.dto.user.detailed;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.predman.content.dto.project.ProjectDto;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record UserProjectsDto(
        UUID id,

        String login,

        String email,

        @JsonProperty(value = "joined_projects")
        List<ProjectDto> joinedProjects
)
{ }
