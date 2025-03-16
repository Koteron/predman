package com.predman.content.auth;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private static final long EXPIRATION_TIME = 86400000;

    private final Key key;

    JwtTokenProvider() throws IOException {
        String secretKey;
        ClassPathResource resource = new ClassPathResource(".config");
        String[] parts = Files.readAllLines(resource.getFile().toPath()).getFirst().split(":", 2);
        if (parts[0].equals("JWT_KEY")) {
            secretKey = parts[1];
        }
        else {
            throw new JwtException("Not found JWT secret key");
        }
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        Jwts.parser().verifyWith((SecretKey) key).build().parse(token);
        return true;
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
