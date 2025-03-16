package com.predman.content.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordUtil {
    private final PasswordEncoder encoder;

    public String encrypt(String password) {
        return encoder.encode(password);
    }

    public boolean matches(String password, String encryptedPassword) {
        return encoder.matches(password, encryptedPassword);
    }
}
