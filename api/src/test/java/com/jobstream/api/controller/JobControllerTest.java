package com.jobstream.api.controller;

import com.jobstream.api.config.TestContainerConfig;
import com.jobstream.api.entity.Job;
import com.jobstream.api.entity.Role;
import com.jobstream.api.entity.User;
import com.jobstream.api.repository.JobRepository;
import com.jobstream.api.repository.UserRepository;
import com.jobstream.api.service.JwtService;
import com.jobstream.endpoint.JobApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
@Transactional
class JobControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User alice;
    private User bob;
    private String aliceToken;
    private String bobToken;

    private static final String JOB_JSON = """
            {
              "externalId": "job_1",
              "title": "Java Developer",
              "company": "TechCorp",
              "location": "Paris"
            }
            """;

    @Test
    void savedOfferOperations_shouldReceiveUserThroughAuthenticationPrincipal() {
        assertThat(JobApi.class.getDeclaredMethods()).hasSize(5);
        for (var method : JobApi.class.getDeclaredMethods()) {
            var parameters = method.getParameters();
            var principal = parameters[parameters.length - 1];
            assertThat(principal.getType()).isEqualTo(User.class);
            assertThat(principal.isAnnotationPresent(AuthenticationPrincipal.class)).isTrue();
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        alice = createUser("alice@example.com");
        bob = createUser("bob@example.com");
        aliceToken = jwtService.generateToken(alice);
        bobToken = jwtService.generateToken(bob);
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("unused");
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    private Job createJob(String externalId, User owner) {
        Job job = new Job();
        job.setExternalId(externalId);
        job.setTitle("Java Developer");
        job.setCompany("TechCorp");
        job.setLocation("Paris");
        job.setUser(owner);
        return jobRepository.saveAndFlush(job);
    }

    private MockHttpServletRequestBuilder asAlice(MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + aliceToken);
    }

    @Test
    void getJobByExternalId_shouldReturnOnlyAuthenticatedOwnersMatchingOffer() throws Exception {
        createJob("shared-external-id", bob);
        Job offer = createJob("shared-external-id", alice);

        mockMvc.perform(asAlice(get("/jobs/by-external-id/{externalId}", "shared-external-id")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(offer.getId()))
                .andExpect(jsonPath("$.externalId").value("shared-external-id"))
                .andExpect(jsonPath("$.title").value("Java Developer"));
    }

    @Test
    void getJobByExternalId_shouldReturn404ForForeignOrMissingOffer() throws Exception {
        createJob("foreign-offer", bob);

        for (String externalId : new String[]{"foreign-offer", "missing-offer"}) {
            mockMvc.perform(asAlice(get("/jobs/by-external-id/{externalId}", externalId))
                            .param("userId", bob.getId().toString()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Job not found"));
        }
    }

    @Test
    void getJobById_shouldReturnOwnersOffer() throws Exception {
        Job offer = createJob("job_1", alice);
        mockMvc.perform(asAlice(get("/jobs/" + offer.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(offer.getId()))
                .andExpect(jsonPath("$.externalId").value("job_1"))
                .andExpect(jsonPath("$.title").value("Java Developer"));
    }

    @Test
    void getJobById_shouldReturn404ForForeignOrMissingOffer() throws Exception {
        Job foreign = createJob("job_1", bob);
        for (Long id : new Long[]{foreign.getId(), Long.MAX_VALUE}) {
            mockMvc.perform(asAlice(get("/jobs/" + id)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Job not found"));
        }
    }

    @Test
    void getJobs_shouldReturnOnlyPersonalCollectionWithDefaultPagination() throws Exception {
        createJob("alice-1", alice);
        createJob("bob-1", bob);
        mockMvc.perform(asAlice(get("/jobs")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].externalId").value("alice-1"));
    }

    @Test
    void getJobs_shouldPreservePageAndSortWithinPersonalCollection() throws Exception {
        createJob("alice-2", alice);
        createJob("alice-1", alice);
        createJob("bob-1", bob);
        mockMvc.perform(asAlice(get("/jobs").param("page", "1").param("size", "1")
                        .param("sort", "externalId,asc")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].externalId").value("alice-2"));
    }

    @Test
    void getJobs_shouldApplyDescendingDirectionFromSingleSortParameter() throws Exception {
        createJob("alice-1", alice);
        createJob("alice-2", alice);
        mockMvc.perform(asAlice(get("/jobs").param("sort", "externalId,desc")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].externalId").value("alice-2"))
                .andExpect(jsonPath("$.content[1].externalId").value("alice-1"));
    }

    @Test
    void getJobs_shouldPreserveRepeatedSortCriteria() throws Exception {
        createJob("alice-1", alice);
        createJob("alice-2", alice);
        mockMvc.perform(asAlice(get("/jobs").param("sort", "title,asc", "externalId,desc")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].externalId").value("alice-2"))
                .andExpect(jsonPath("$.content[1].externalId").value("alice-1"));
    }

    @Test
    void saveJob_shouldReturn201AndAttachAuthenticatedOwner() throws Exception {
        var result = mockMvc.perform(asAlice(post("/jobs"))
                        .param("userId", bob.getId().toString()).param("id", bob.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON).content(JOB_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        assertThat(result.getResponse().getHeader("Location")).endsWith("/jobs/" + id);
        mockMvc.perform(asAlice(get("/jobs/" + id))).andExpect(status().isOk());
        mockMvc.perform(get("/jobs/" + id).header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void saveJob_shouldReturn400WhenBodyInvalid() throws Exception {
        mockMvc.perform(asAlice(post("/jobs")).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void saveJob_shouldReturn409OnlyForSameUsersDuplicate() throws Exception {
        mockMvc.perform(asAlice(post("/jobs")).contentType(MediaType.APPLICATION_JSON).content(JOB_JSON))
                .andExpect(status().isCreated());
        mockMvc.perform(asAlice(post("/jobs")).contentType(MediaType.APPLICATION_JSON).content(JOB_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Resource conflict"));
        mockMvc.perform(post("/jobs").header("Authorization", "Bearer " + bobToken)
                        .contentType(MediaType.APPLICATION_JSON).content(JOB_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void deleteJob_shouldRemoveOwnersOffer() throws Exception {
        Job offer = createJob("job_1", alice);
        mockMvc.perform(asAlice(delete("/jobs/" + offer.getId()))).andExpect(status().isNoContent());
        mockMvc.perform(asAlice(get("/jobs/" + offer.getId()))).andExpect(status().isNotFound());
    }

    @Test
    void deleteJob_shouldReturn404AndPreserveForeignOffer() throws Exception {
        Job foreign = createJob("job_1", bob);
        mockMvc.perform(asAlice(delete("/jobs/" + foreign.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        mockMvc.perform(get("/jobs/" + foreign.getId()).header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk());
        mockMvc.perform(asAlice(delete("/jobs/" + Long.MAX_VALUE))).andExpect(status().isNotFound());
    }

    @Test
    void allOperations_shouldRequireAuthentication() throws Exception {
        for (var request : new MockHttpServletRequestBuilder[]{
                get("/jobs"), get("/jobs/1"), get("/jobs/by-external-id/job_1"), delete("/jobs/1"),
                post("/jobs").contentType(MediaType.APPLICATION_JSON).content(JOB_JSON)}) {
            mockMvc.perform(request).andExpect(status().isUnauthorized());
        }
    }
}
