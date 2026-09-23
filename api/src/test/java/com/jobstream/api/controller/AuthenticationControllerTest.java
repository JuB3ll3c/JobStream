package com.jobstream.api.controller;

import com.jobstream.api.config.RestAuthenticationEntryPoint;
import com.jobstream.api.exception.ResourceConflictException;
import com.jobstream.api.repository.UserRepository;
import com.jobstream.api.service.AuthenticationService;
import com.jobstream.api.service.JwtService;
import com.jobstream.dto.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    // Security dependencies required to load ApplicationContext in @WebMvcTest slice
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    private static final String REGISTER_JSON = """
            {
              "email": "alice@test.com",
              "password": "password123",
              "firstName": "Alice",
              "lastName": "Dupont"
            }
            """;

    private static final String LOGIN_JSON = """
            {
              "email": "alice@test.com",
              "password": "password123"
            }
            """;

    @Test
    void register_shouldReturn200WithAuthResponse_whenValid() throws Exception {
        AuthResponse response = new AuthResponse();
        response.setAccessToken("jwt-token");
        response.setTokenType("Bearer");
        when(authenticationService.register(any())).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void register_shouldReturn400_whenBodyInvalid_missingFields() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenPasswordTooShort() throws Exception {
        String shortPwd = """
                {
                  "email": "alice@test.com",
                  "password": "short",
                  "firstName": "Alice",
                  "lastName": "Dupont"
                }
                """;
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shortPwd))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenEmailInvalid() throws Exception {
        String badEmail = """
                {
                  "email": "not-an-email",
                  "password": "password123",
                  "firstName": "Alice",
                  "lastName": "Dupont"
                }
                """;
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badEmail))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn409_whenConflict() throws Exception {
        when(authenticationService.register(any()))
                .thenThrow(new ResourceConflictException("An account already exists with this email."));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void login_shouldReturn200WithAuthResponse_whenValid() throws Exception {
        AuthResponse response = new AuthResponse();
        response.setAccessToken("jwt-login");
        response.setTokenType("Bearer");
        when(authenticationService.login(any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-login"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_shouldReturn400_whenBodyInvalid() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
