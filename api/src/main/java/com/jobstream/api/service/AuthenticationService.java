package com.jobstream.api.service;

import com.jobstream.api.entity.Role;
import com.jobstream.api.entity.User;
import com.jobstream.api.exception.ResourceConflictException;
import com.jobstream.api.exception.ResourceNotFoundException;
import com.jobstream.api.repository.UserRepository;
import com.jobstream.dto.AuthResponse;
import com.jobstream.dto.LoginRequest;
import com.jobstream.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("An account already exists with this email.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(Role.USER);

        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);

        AuthResponse response = new AuthResponse();
        response.setAccessToken(jwtToken);
        response.setTokenType("Bearer");
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();

        String jwtToken = jwtService.generateToken(user);

        AuthResponse response = new AuthResponse();
        response.setAccessToken(jwtToken);
        response.setTokenType("Bearer");
        return response;
    }
}
