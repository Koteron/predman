package com.predman.content.auth;

import com.predman.content.entity.User;
import com.predman.content.mapper.UserMapper;
import com.predman.content.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;
    private final UserMapper userMapper;

    public CustomUserDetailsService(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userMapper.convertToUserEntity(userService.getByEmail(email));

        return new CustomUserDetails(user);
    }
}
