package com.predman.content.controller;

import com.predman.content.dto.project.ProjectDto;
import com.predman.content.dto.user.auth.UserAuthResponseDto;
import com.predman.content.dto.user.auth.UserLoginDto;
import com.predman.content.dto.user.auth.UserRegisterDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.dto.user.detailed.UserProjectsDto;
import com.predman.content.entity.User;
import com.predman.content.mapper.UserMapper;
import com.predman.content.service.ProjectMemberService;
import com.predman.content.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final ProjectMemberService projectMemberService;

    @PostMapping("/register")
    public UserAuthResponseDto register(@Valid @RequestBody UserRegisterDto userRegisterDto)
    {
        return userService.register(userRegisterDto);
    }

    @PostMapping("/login")
    public UserAuthResponseDto login(@Valid @RequestBody UserLoginDto userLoginDto)
    {
        return userService.login(userLoginDto);
    }

    @GetMapping("/info")
    public UserProjectsDto getFullUserInfo()
    {
        User user = userService.getAuthenticatedUser();
        List<ProjectDto> joinedProjects = projectMemberService.getProjectsByUserId(
                userService.getAuthenticatedUser().getId());
        return UserProjectsDto
                .builder()
                .id(user.getId())
                .email(user.getEmail())
                .login(user.getLogin())
                .joinedProjects(joinedProjects)
                .build();
    }

    @GetMapping
    public UserDto getUserById()
    {
        return userMapper.convertToUserDto(userService.getAuthenticatedUser());
    }

    @DeleteMapping
    public void deleteUserById()
    {
        userService.deleteById(userService.getAuthenticatedUser().getId());
    }
}
