package com.jobstream.api.config;

import com.jobstream.api.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(authenticationEntryPoint, jwtService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_shouldPassThrough_whenNoAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_shouldPassThrough_whenNotBearerPrefix() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Token abc.def.ghi");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_shouldAuthenticate_whenValidTokenAndNoExistingAuth() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("alice@test.com");
        UserDetails userDetails = new User("alice@test.com", "pwd", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userDetailsService.loadUserByUsername("alice@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid.jwt.token", userDetails)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userDetails);
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
    }

    @Test
    void doFilter_shouldNotAuthenticate_whenTokenInvalid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
        when(jwtService.extractUsername("invalid.token")).thenReturn("alice@test.com");
        UserDetails userDetails = new User("alice@test.com", "pwd", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userDetailsService.loadUserByUsername("alice@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("invalid.token", userDetails)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_shouldNotOverwrite_whenAlreadyAuthenticated() throws Exception {
        // Pre-authenticate context
        UserDetails existing = new User("bob@test.com", "pwd", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(existing, null, existing.getAuthorities())
        );

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("alice@test.com");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // Should not reload user nor validate token
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(jwtService, never()).isTokenValid(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(existing);
    }

    @Test
    void doFilter_shouldDelegateToAuthenticationEntryPoint_whenExtractThrows() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad.token");
        when(jwtService.extractUsername("bad.token")).thenThrow(new JwtException("invalid jwt"));

        filter.doFilterInternal(request, response, filterChain);

        verify(authenticationEntryPoint).commence(eq(request), eq(response), any(AuthenticationException.class));
        verify(filterChain, never()).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_shouldDelegateToAuthenticationEntryPoint_whenUserNotFound() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token");
        when(jwtService.extractUsername("valid.token")).thenReturn("unknown@test.com");
        when(userDetailsService.loadUserByUsername("unknown@test.com"))
                .thenThrow(new UsernameNotFoundException("not found"));

        filter.doFilterInternal(request, response, filterChain);

        verify(authenticationEntryPoint).commence(eq(request), eq(response), any(AuthenticationException.class));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_shouldPropagateUnexpectedException_whenLoadingUserFails() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token");
        when(jwtService.extractUsername("valid.token")).thenReturn("alice@test.com");
        when(userDetailsService.loadUserByUsername("alice@test.com"))
                .thenThrow(new IllegalArgumentException("unexpected user data"));

        assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("unexpected user data");

        verifyNoInteractions(authenticationEntryPoint);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_shouldDelegateToAuthenticationEntryPoint_whenIsTokenValidThrows() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token");
        when(jwtService.extractUsername("valid.token")).thenReturn("alice@test.com");
        UserDetails userDetails = new User("alice@test.com", "pwd", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userDetailsService.loadUserByUsername("alice@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid.token", userDetails)).thenThrow(new JwtException("validation error"));

        filter.doFilterInternal(request, response, filterChain);

        verify(authenticationEntryPoint).commence(eq(request), eq(response), any(AuthenticationException.class));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_shouldHandleNullUsernameExtracted() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token");
        when(jwtService.extractUsername("valid.token")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_shouldPropagateDownstreamException_whenTokenIsValid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("alice@test.com");
        UserDetails userDetails = new User("alice@test.com", "pwd", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userDetailsService.loadUserByUsername("alice@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid.jwt.token", userDetails)).thenReturn(true);
        doThrow(new ServletException("downstream failure"))
                .when(filterChain).doFilter(request, response);

        assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(ServletException.class)
                .hasMessage("downstream failure");

        verifyNoInteractions(authenticationEntryPoint);
    }
}
