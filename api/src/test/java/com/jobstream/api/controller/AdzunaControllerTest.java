package com.jobstream.api.controller;

import com.jobstream.api.config.RestAuthenticationEntryPoint;
import com.jobstream.api.exception.ResourceNotFoundException;
import com.jobstream.api.repository.UserRepository;
import com.jobstream.api.service.AdzunaService;
import com.jobstream.api.service.JwtService;
import com.jobstream.dto.JobDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdzunaController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdzunaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdzunaService adzunaService;

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

    @MockitoBean
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @Test
    void legacySearch_shouldBeUnavailable() throws Exception {
        mockMvc.perform(get("/adzuna/jobs").param("query", "java"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getJobById_shouldReturn200WithJob() throws Exception {
        JobDto job = new JobDto("job_1", "Java Developer", "TechCorp", "Paris, France");
        job.setDescription("Full description");
        job.setSalaryMin(50000);
        job.setSalaryMax(80000);

        when(adzunaService.getJobById("job_1")).thenReturn(job);

        mockMvc.perform(get("/adzuna/jobs/job_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("job_1"))
                .andExpect(jsonPath("$.title").value("Java Developer"))
                .andExpect(jsonPath("$.company").value("TechCorp"))
                .andExpect(jsonPath("$.location").value("Paris, France"));
    }

    @Test
    void getJobById_shouldReturn404WhenNotFound() throws Exception {
        when(adzunaService.getJobById("job_unknown"))
                .thenThrow(new ResourceNotFoundException("Job not found with id: job_unknown"));

        mockMvc.perform(get("/adzuna/jobs/job_unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Job not found"));
    }
}
