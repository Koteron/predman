package com.predman.content.auth;

import com.predman.content.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserDetailsService userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String email = "test@example.com";
    private final String token = "valid.jwt.token";

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_withValidToken_setsAuthentication() throws ServletException, IOException {
        UserDetails userDetails = new CustomUserDetails(
                User.builder().email(email).passwordHash("password").build());

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.extractEmail(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertInstanceOf(UsernamePasswordAuthenticationToken.class, authentication);
        assertEquals(email, authentication.getName());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withNoToken_doesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withInvalidToken_doesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void getTokenFromRequest_shouldExtractTokenProperly() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(userDetailsService, jwtTokenProvider);

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer abc.def.ghi");

        var method = JwtAuthenticationFilter.class
                .getDeclaredMethod("getTokenFromRequest", HttpServletRequest.class);
        method.setAccessible(true);

        String extractedToken = (String) method.invoke(filter, mockRequest);

        assertEquals("abc.def.ghi", extractedToken);
    }

}