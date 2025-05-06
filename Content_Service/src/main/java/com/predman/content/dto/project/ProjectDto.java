package com.predman.content.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ProjectDto(

    UUID id,

    String name,

    String description,

    @JsonProperty(value = "owner_id")
    UUID ownerId
) { }
