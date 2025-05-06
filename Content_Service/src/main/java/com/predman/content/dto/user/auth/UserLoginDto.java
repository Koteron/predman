package com.predman.content.dto.user.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserLoginDto(

        @NotNull
        String password,

        @NotNull
        String email
) { }