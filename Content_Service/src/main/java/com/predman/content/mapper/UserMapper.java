package com.predman.content.mapper;

import com.predman.content.auth.CustomUserDetails;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.User;
import com.predman.content.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    public UserDto convertToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .login(user.getLogin())
                .build();
    }

    public User convertToUserEntity(UserDto userDto) {
        return User.builder()
                .id(userDto.id())
                .email(userDto.email())
                .login(userDto.login())
                .build();
    }

    public User convertToUserEntity(CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            throw new ForbiddenException("Authentication details are missing!");
        }
        if (customUserDetails.getUserId() == null) {
            throw new ForbiddenException("User ID is missing in authentication details!");
        }
        return User.builder()
                .id(customUserDetails.getUserId())
                .email(customUserDetails.getUsername())
                .login(customUserDetails.getLogin())
                .passwordHash(customUserDetails.getPassword())
                .build();
    }
}
