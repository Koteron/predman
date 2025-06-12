package com.predman.content.dto.user.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserRegisterDto(
        @NotNull
        String login,

        @NotNull
        String password,

        @NotNull
        String email
) { }
