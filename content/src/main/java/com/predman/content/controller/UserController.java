package com.predman.content.controller;

import com.predman.content.dto.user.auth.UserAuthResponseDto;
import com.predman.content.dto.user.auth.UserLoginDto;
import com.predman.content.dto.user.auth.UserRegisterDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.mapper.UserMapper;
import com.predman.content.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

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
