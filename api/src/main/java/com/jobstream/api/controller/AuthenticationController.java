package com.jobstream.api.controller;

import com.jobstream.api.service.AuthenticationService;
import com.jobstream.dto.AuthResponse;
import com.jobstream.dto.LoginRequest;
import com.jobstream.dto.RegisterRequest;
import com.jobstream.endpoint.AuthenticationApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticationController implements AuthenticationApi {
    private final AuthenticationService authenticationService;

    @Override
    public ResponseEntity<AuthResponse> register(RegisterRequest registerRequest) {
        AuthResponse response = authenticationService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AuthResponse> login(LoginRequest loginRequest) {
        AuthResponse response = authenticationService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}
