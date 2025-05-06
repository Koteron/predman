package com.predman.content.dto.user.auth;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserAuthResponseDto(
    UUID id,

    String login,

    String email,

    String token
) { }
