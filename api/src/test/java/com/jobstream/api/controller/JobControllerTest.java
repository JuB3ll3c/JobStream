package com.jobstream.api.controller;

import com.jobstream.api.exception.ResourceConflictException;
import com.jobstream.api.exception.ResourceNotFoundException;
import com.jobstream.api.service.JobService;
import com.jobstream.dto.JobDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static final String JOB_JSON = """
            {
              "externalId": "job_1",
              "title": "Développeur Java",
              "company": "TechCorp",
              "location": "Paris"
            }
            """;

    @Test
    void getJobById_shouldReturn200WithJob() throws Exception {
        JobDto job = new JobDto("job_1", "Développeur Java", "TechCorp", "Paris");
        job.setId(42L);

        when(jobService.getJobById(42L)).thenReturn(job);

        mockMvc.perform(get("/jobs/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.externalId").value("job_1"))
                .andExpect(jsonPath("$.title").value("Développeur Java"));
    }

    @Test
    void getJobById_shouldReturn404WhenNotFound() throws Exception {
        when(jobService.getJobById(42L))
                .thenThrow(new ResourceNotFoundException("Job non trouvé avec l'id : 42"));

        mockMvc.perform(get("/jobs/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Offre non trouvée"));
    }

    @Test
    void getJobs_shouldReturn200WithPagedResponse() throws Exception {
        JobDto job = new JobDto("job_1", "Développeur Java", "TechCorp", "Paris");
        PageImpl<JobDto> page = new PageImpl<>(List.of(job), PageRequest.of(0, 20), 1);

        when(jobService.getJobs(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/jobs").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.content[0].externalId").value("job_1"));
    }

    @Test
    void saveJob_shouldReturn201WithLocation() throws Exception {
        JobDto saved = new JobDto("job_1", "Développeur Java", "TechCorp", "Paris");
        saved.setId(42L);

        when(jobService.saveJob(any())).thenReturn(saved);

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JOB_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/jobs/42")))
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    void saveJob_shouldReturn400WhenBodyInvalid() throws Exception {
        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void saveJob_shouldReturn409WhenAlreadySaved() throws Exception {
        when(jobService.saveJob(any()))
                .thenThrow(new ResourceConflictException("Job déjà sauvegardé"));

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JOB_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflit de ressources"));
    }

    @Test
    void deleteJob_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/jobs/42"))
                .andExpect(status().isNoContent());

        verify(jobService).deleteJob(42L);
    }

    @Test
    void deleteJob_shouldReturn404WhenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Job non trouvé avec l'id : 42"))
                .when(jobService).deleteJob(42L);

        mockMvc.perform(delete("/jobs/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}