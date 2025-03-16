package com.predman.content.dto.user.detailed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserDto(
    UUID id,

    String login,

    @JsonIgnore
    String passwordHash,

    String email
)
{ }
