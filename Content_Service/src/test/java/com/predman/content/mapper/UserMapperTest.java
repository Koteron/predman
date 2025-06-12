package com.predman.content.mapper;

import com.predman.content.auth.CustomUserDetails;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.User;
import com.predman.content.exception.ForbiddenException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void convertToUserDto_shouldMapEntityToDtoCorrectly() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .login("testuser")
                .build();

        UserDto dto = userMapper.convertToUserDto(user);

        assertEquals(userId, dto.id());
        assertEquals("test@example.com", dto.email());
        assertEquals("testuser", dto.login());
    }

    @Test
    void convertToUserEntity_shouldMapDtoToEntityCorrectly() {
        UUID userId = UUID.randomUUID();
        UserDto dto = UserDto.builder()
                .id(userId)
                .email("user@example.com")
                .login("loginUser")
                .build();

        User user = userMapper.convertToUserEntity(dto);

        assertEquals(userId, user.getId());
        assertEquals("user@example.com", user.getEmail());
        assertEquals("loginUser", user.getLogin());
    }

    @Test
    void convertToUserEntity_shouldMapCustomUserDetailsToEntityCorrectly() {
        UUID userId = UUID.randomUUID();
        CustomUserDetails details = new CustomUserDetails(
                User.builder()
                        .id(userId)
                        .passwordHash("securepassword")
                        .email("custom@example.com")
                        .login("customLogin")
                        .build()
        );

        User user = userMapper.convertToUserEntity(details);

        assertEquals(userId, user.getId());
        assertEquals("custom@example.com", user.getEmail());
        assertEquals("customLogin", user.getLogin());
        assertEquals("securepassword", user.getPasswordHash());
    }

    @Test
    void convertToUserEntity_shouldThrowForbidden_whenCustomUserDetailsIsNull() {
        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> userMapper.convertToUserEntity((CustomUserDetails) null)
        );

        assertEquals("Authentication details are missing!", ex.getMessage());
    }

    @Test
    void convertToUserEntity_shouldThrowForbidden_whenUserIdIsNull() {
        CustomUserDetails details = new CustomUserDetails(
                User.builder()
                        .id(null)
                        .passwordHash("pass")
                        .email("email@example.com")
                        .login("login")
                        .build()
        );

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> userMapper.convertToUserEntity(details)
        );

        assertEquals("User ID is missing in authentication details!", ex.getMessage());
    }
}
