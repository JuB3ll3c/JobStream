package com.jobstream.api.config;

import com.jobstream.api.controller.AuthenticationController;
import com.jobstream.api.controller.JobController;
import com.jobstream.api.repository.UserRepository;
import com.jobstream.api.service.AuthenticationService;
import com.jobstream.api.service.JobService;
import com.jobstream.api.service.JwtService;
import com.jobstream.dto.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthenticationController.class, JobController.class})
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestAccessDeniedHandler accessDeniedHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JobService jobService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @Test
    void protectedEndpoint_shouldReturnUnauthorizedErrorResponse_whenAuthenticationIsMissing() throws Exception {
        mockMvc.perform(get("/jobs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.date").isNotEmpty());
    }

    @Test
    void protectedEndpoint_shouldReturnUnauthorizedErrorResponse_whenBearerTokenIsInvalid() throws Exception {
        when(jwtService.extractUsername("invalid-token"))
                .thenThrow(new IllegalArgumentException("Invalid token"));

        mockMvc.perform(get("/jobs")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.date").isNotEmpty());
    }

    @Test
    void accessDeniedHandler_shouldReturnForbiddenErrorResponse() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(
                new MockHttpServletRequest(),
                response,
                new AccessDeniedException("Forbidden")
        );

        JsonNode body = objectMapper.readTree(response.getContentAsByteArray());
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(body.get("status").asInt()).isEqualTo(403);
        assertThat(body.get("error").asString()).isEqualTo("Forbidden");
        assertThat(body.get("message").asString()).isEqualTo("Access denied");
        assertThat(body.get("date").asString()).isNotBlank();
    }

    @Test
    void loginEndpoint_shouldRemainPublic() throws Exception {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("jwt-token");
        authResponse.setTokenType("Bearer");
        when(authenticationService.login(any())).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "alice@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }
}
