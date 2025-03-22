package com.predman.content.auth;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private static final long EXPIRATION_TIME = 86400000;

    private final Key key;

    JwtTokenProvider(@Value("classpath:.config") Resource jwtConfig) throws IOException {
        String secretKey;
        try (InputStream is = jwtConfig.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line = reader.readLine();
            String[] parts = line.strip().split(":", 2);

            if (parts.length == 2 && parts[0].equals("JWT_KEY")) {
                secretKey = parts[1];
            } else {
                throw new JwtException("Not found JWT secret key");
            }

            this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        }
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
