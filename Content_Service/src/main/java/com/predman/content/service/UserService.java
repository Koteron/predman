package com.predman.content.service;

import com.predman.content.dto.user.auth.UserAuthResponseDto;
import com.predman.content.dto.user.auth.UserLoginDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.dto.user.auth.UserRegisterDto;
import com.predman.content.entity.User;

import java.util.UUID;

public interface UserService {
    User getEntityById(UUID id);
    User getEntityByEmail(String email);
    UserDto getById(UUID id);
    UserDto getByEmail(String email);
    UserAuthResponseDto login(UserLoginDto loginDto);
    UserAuthResponseDto register(UserRegisterDto userDto);
    void deleteById(UUID userId);
    User getAuthenticatedUser();
    void checkAuthenticatedUser(UUID userId);
}
