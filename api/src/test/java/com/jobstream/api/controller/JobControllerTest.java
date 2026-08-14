package com.jobstream.api.controller;

import com.jobstream.api.exception.ExternalApiException;
import com.jobstream.api.exception.ResourceNotFoundException;
import com.jobstream.api.service.JobService;
import com.jobstream.dto.JobDto;
import com.jobstream.dto.JobSearchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    @Test
    void searchJobs_shouldReturn200WithJobs() throws Exception {
        JobDto job = new JobDto("job_1", "Développeur Java", "TechCorp", "Paris, France");
        job.setDescription("Description");
        job.setSalaryMin(50000);
        job.setSalaryMax(80000);
        job.setContractType("CDI");
        job.setPostedDate(LocalDate.of(2026, 3, 25));
        job.setJobUrl("https://example.com/job/1");

        JobSearchResponse response = new JobSearchResponse();
        response.setJobs(List.of(job));
        response.setTotal(150);
        response.setCount(1);

        when(jobService.searchJobs("java", 1, 20, null)).thenReturn(response);

        mockMvc.perform(get("/api/jobs").param("query", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(150))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.jobs[0].externalId").value("job_1"))
                .andExpect(jsonPath("$.jobs[0].title").value("Développeur Java"))
                .andExpect(jsonPath("$.jobs[0].company").value("TechCorp"))
                .andExpect(jsonPath("$.jobs[0].location").value("Paris, France"));
    }

    @Test
    void searchJobs_shouldReturn400WhenQueryBlank() throws Exception {
        mockMvc.perform(get("/api/jobs").param("query", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Paramètres de requête invalides"));
    }

    @Test
    void searchJobs_shouldReturn400WhenQueryTooLong() throws Exception {
        String longQuery = "a".repeat(201);

        mockMvc.perform(get("/api/jobs").param("query", longQuery))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchJobs_shouldReturn400WhenPageBelowMin() throws Exception {
        mockMvc.perform(get("/api/jobs").param("query", "java").param("page", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchJobs_shouldReturn400WhenLimitAboveMax() throws Exception {
        mockMvc.perform(get("/api/jobs").param("query", "java").param("limit", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchJobs_shouldReturn400WhenLocationInvalid() throws Exception {
        mockMvc.perform(get("/api/jobs").param("query", "java").param("location", "paris!!"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchJobs_shouldReturn400WhenPageIsNotANumber() throws Exception {
        mockMvc.perform(get("/api/jobs").param("query", "java").param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Type de paramètre invalide"));
    }

    @Test
    void searchJobs_shouldReturn502WhenExternalApiFails() throws Exception {
        when(jobService.searchJobs("java", 1, 20, null))
                .thenThrow(new ExternalApiException("Adzuna", "Erreur lors de la recherche d'offres", 502));

        mockMvc.perform(get("/api/jobs").param("query", "java"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.error").value("Erreur lors de la communication avec le service externe"))
                .andExpect(jsonPath("$.message").value("Erreur lors de la recherche d'offres"));
    }

    @Test
    void searchJobs_shouldReturn500OnUnexpectedError() throws Exception {
        when(jobService.searchJobs("java", 1, 20, null))
                .thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(get("/api/jobs").param("query", "java"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Erreur interne du serveur"));
    }

    @Test
    void getJobById_shouldReturn200WithJob() throws Exception {
        JobDto job = new JobDto("job_1", "Développeur Java", "TechCorp", "Paris, France");
        job.setDescription("Description complète");
        job.setSalaryMin(50000);
        job.setSalaryMax(80000);

        when(jobService.getJobById("job_1")).thenReturn(job);

        mockMvc.perform(get("/api/jobs/job_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("job_1"))
                .andExpect(jsonPath("$.title").value("Développeur Java"))
                .andExpect(jsonPath("$.company").value("TechCorp"))
                .andExpect(jsonPath("$.location").value("Paris, France"));
    }

    @Test
    void getJobById_shouldReturn404WhenNotFound() throws Exception {
        when(jobService.getJobById("job_unknown"))
                .thenThrow(new ResourceNotFoundException("Offre introuvable avec l'id: job_unknown"));

        mockMvc.perform(get("/api/jobs/job_unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Offre non trouvée"));
    }
}