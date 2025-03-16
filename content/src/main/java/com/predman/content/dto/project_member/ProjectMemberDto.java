package com.predman.content.dto.project_member;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;

import java.util.UUID;

@Builder
public record ProjectMemberDto(
    @NonNull
    @JsonProperty(value = "user_id")
    UUID userId,

    @NonNull
    @JsonProperty(value = "project_id")
    UUID projectId
) { }
