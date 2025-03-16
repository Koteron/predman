package com.predman.content.dto.project_member;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ProjectMemberUpdateDto(
        @JsonProperty(value = "project_id")
        @NotNull
        UUID projectId,

        @JsonProperty(value = "user_email")
        @NotNull
        String userEmail,

        @JsonProperty(value = "user_Id")
        UUID userId
) {
}
