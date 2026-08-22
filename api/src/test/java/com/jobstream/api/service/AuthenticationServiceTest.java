package com.jobstream.api.service;

import com.jobstream.api.entity.Role;
import com.jobstream.api.entity.User;
import com.jobstream.api.exception.ResourceConflictException;
import com.jobstream.api.repository.UserRepository;
import com.jobstream.dto.AuthResponse;
import com.jobstream.dto.LoginRequest;
import com.jobstream.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_shouldThrowConflict_whenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("alice@test.com");
        request.setPassword("password123");
        request.setFirstName("Alice");
        request.setLastName("Dupont");

        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_shouldEncodePasswordSaveUserWithRoleUserAndReturnBearerToken() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("bob@test.com");
        request.setPassword("secret123");
        request.setFirstName("Bob");
        request.setLastName("Martin");

        when(userRepository.existsByEmail("bob@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded123");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token-123");

        AuthResponse response = authenticationService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("bob@test.com");
        assertThat(saved.getPassword()).isEqualTo("encoded123");
        assertThat(saved.getFirstName()).isEqualTo("Bob");
        assertThat(saved.getLastName()).isEqualTo("Martin");
        assertThat(saved.getRole()).isEqualTo(Role.USER);

        verify(passwordEncoder).encode("secret123");
        verify(jwtService).generateToken(any(User.class));

        assertThat(response.getAccessToken()).isEqualTo("jwt-token-123");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void register_shouldGenerateTokenForSavedUser() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("eve@test.com");
        request.setPassword("password123");
        request.setFirstName("Eve");
        request.setLastName("Test");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("enc");
        when(jwtService.generateToken(any(User.class))).thenReturn("tok");

        authenticationService.register(request);

        verify(jwtService).generateToken(argThat(userDetails ->
                userDetails.getUsername().equals("eve@test.com")));
    }

    @Test
    void login_shouldAuthenticateAndReturnToken_whenCredentialsValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@test.com");
        request.setPassword("password");

        User user = new User();
        user.setEmail("alice@test.com");
        user.setRole(Role.USER);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtService.generateToken(user)).thenReturn("jwt-valid");

        AuthResponse response = authenticationService.login(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getPrincipal()).isEqualTo("alice@test.com");
        assertThat(tokenCaptor.getValue().getCredentials()).isEqualTo("password");

        assertThat(response.getAccessToken()).isEqualTo("jwt-valid");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void login_shouldPropagateBadCredentials_whenAuthenticationFails() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@test.com");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }
}
