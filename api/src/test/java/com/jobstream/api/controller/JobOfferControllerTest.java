package com.jobstream.api.controller;

import com.jobstream.api.client.AdzunaClient;
import com.jobstream.api.config.RestAuthenticationEntryPoint;
import com.jobstream.api.config.RestAccessDeniedHandler;
import com.jobstream.api.config.SecurityConfig;
import com.jobstream.api.exception.ExternalApiException;
import com.jobstream.api.mapper.AdzunaMapper;
import com.jobstream.api.repository.UserRepository;
import com.jobstream.api.service.AdzunaService;
import com.jobstream.api.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.web.FilterChainProxy;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobOfferController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({AdzunaService.class, AdzunaMapper.class, SecurityConfig.class,
        RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class JobOfferControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @MockitoBean
    private AdzunaClient adzunaClient;

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

    @BeforeEach
    void authenticateRequests() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        var user = User.withUsername("user@example.com").password("unused").roles("USER").build();
        when(jwtService.extractUsername("test-token")).thenReturn(user.getUsername());
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(jwtService.isTokenValid("test-token", user)).thenReturn(true);
    }

    private MockHttpServletRequestBuilder searchRequest() {
        return get("/job-offers").header("Authorization", "Bearer test-token");
    }

    @ParameterizedTest
    @CsvSource({"title,''", "title,'   '", "page,0", "page,-1", "page,abc", "size,0",
            "size,101", "size,abc", "location,''", "location,'   '"})
    void search_shouldRejectInvalidParameters(String parameter, String value) throws Exception {
        var request = searchRequest().param("title", "java");
        request.queryParam(parameter, value);
        // Replace title instead of appending another value.
        if (parameter.equals("title")) {
            request = searchRequest().param("title", value);
        }
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(adzunaClient);
    }

    @Test
    void search_shouldRejectOverlongKeywords() throws Exception {
        mockMvc.perform(searchRequest().param("title", "a".repeat(201)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(searchRequest().param("title", "java").param("location", "a".repeat(201)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(adzunaClient);
    }

    @Test
    void search_shouldReturnEmptyCollectionWhenNoOffersMatch() throws Exception {
        when(adzunaClient.callAdzunaApi("java", 1, 20, null))
                .thenReturn(Map.of("count", 0, "results", List.of()));
        mockMvc.perform(searchRequest().param("title", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void search_shouldPreserveTotalsLargerThanIntegerRange() throws Exception {
        when(adzunaClient.callAdzunaApi("java", 1, 20, null))
                .thenReturn(Map.of("count", 3_000_000_000L, "results", List.of()));
        mockMvc.perform(searchRequest().param("title", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3_000_000_000L))
                .andExpect(jsonPath("$.totalPages").value(150_000_000));
    }

    @Test
    void search_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/job-offers").param("title", "java"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(adzunaClient);
    }

    @Test
    void search_shouldRejectInvalidSessionBeforeCallingProvider() throws Exception {
        when(jwtService.extractUsername("expired-token"))
                .thenThrow(new IllegalArgumentException("Expired token"));
        mockMvc.perform(get("/job-offers").param("title", "java")
                        .header("Authorization", "Bearer expired-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        verifyNoInteractions(adzunaClient);
    }

    @ParameterizedTest
    @MethodSource("invalidProviderResponses")
    void search_shouldRejectInvalidProviderResponse(Map<String, Object> response) throws Exception {
        when(adzunaClient.callAdzunaApi("java", 1, 20, null)).thenReturn(response);

        mockMvc.perform(searchRequest().param("title", "java"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Job search is temporarily unavailable"));
    }

    static Stream<Map<String, Object>> invalidProviderResponses() {
        return Stream.of(null, Map.of(), Map.of("count", -1, "results", List.of()),
                Map.of("count", 10, "results", "invalid"));
    }

    @Test
    void search_shouldRejectMissingTitle() throws Exception {
        mockMvc.perform(searchRequest())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void search_shouldHideProviderFailureBehindServiceUnavailable() throws Exception {
        when(adzunaClient.callAdzunaApi("java", 1, 20, null))
                .thenThrow(new ExternalApiException("Adzuna", "what=java app_key=secret", 502));

        mockMvc.perform(searchRequest().param("title", "java"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Service Unavailable"))
                .andExpect(jsonPath("$.message").value("Job search is temporarily unavailable"));
    }

    @Test
    void search_shouldPreserveTotalOnEmptyPageBeyondLastPage() throws Exception {
        when(adzunaClient.callAdzunaApi("java", 5, 10, "Genève")).thenReturn(Map.of(
                "count", 21, "results", List.of()));

        mockMvc.perform(searchRequest().param("title", "java").param("location", "Genève")
                        .param("page", "5").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(5))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(21))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void search_shouldReturnNormalizedResultsWithDefaultPagination() throws Exception {
        when(adzunaClient.callAdzunaApi("java", 1, 20, null)).thenReturn(Map.of(
                "count", 41,
                "results", List.of(Map.of("id", "offer-1", "title", "Backend Engineer",
                        "company", Map.of("display_name", "Acme"),
                        "location", Map.of("display_name", "Zurich")))));

        mockMvc.perform(searchRequest().param("title", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].externalId").value("offer-1"))
                .andExpect(jsonPath("$.content[0].title").value("Backend Engineer"))
                .andExpect(jsonPath("$.content[0].company").value("Acme"))
                .andExpect(jsonPath("$.content[0].location").value("Zurich"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(41))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.jobs").doesNotExist());
    }
}
