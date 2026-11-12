package com.jobstream.api.controller;

import com.jobstream.api.config.TestContainerConfig;
import com.jobstream.api.entity.Role;
import com.jobstream.api.entity.User;
import com.jobstream.api.repository.UserRepository;
import com.jobstream.api.service.JwtService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.ByteArrayOutputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
@Transactional
class CvControllerTest {
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private FilterChainProxy springSecurityFilterChain;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    private MockMvc mockMvc;
    private String token;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        User user = new User();
        user.setEmail("cv-controller@example.com");
        user.setPassword("unused");
        user.setRole(Role.USER);
        token = jwtService.generateToken(userRepository.saveAndFlush(user));
    }

    private MockMultipartFile pdf() throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(output);
            return new MockMultipartFile("file", "cv.pdf", "application/pdf", output.toByteArray());
        }
    }

    @Test
    void upload_shouldCreateThenReplaceAndReturnMetadataOnly() throws Exception {
        mockMvc.perform(multipart("/me/cv").file(pdf()).with(request -> {
            request.setMethod("PUT");
            return request;
        }).header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/me/cv"))
                .andExpect(jsonPath("$.originalFilename").value("cv.pdf"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.created").doesNotExist())
                .andExpect(jsonPath("$.metadata").doesNotExist());
        mockMvc.perform(multipart("/me/cv").file(pdf()).with(request -> {
            request.setMethod("PUT");
            return request;
        }).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing", "empty", "malformed", "too-large", "wrong-type"})
    void upload_shouldReturnContractErrors(String scenario) throws Exception {
        var request = multipart("/me/cv").with(req -> {
            req.setMethod("PUT");
            return req;
        }).header("Authorization", "Bearer " + token);
        int expected = 400;
        if (!scenario.equals("missing")) {
            byte[] content = switch (scenario) {
                case "empty" -> new byte[0];
                case "too-large" -> new byte[5_000_001];
                default -> new byte[]{1, 2, 3};
            };
            String type = scenario.equals("wrong-type") ? "image/png" : "application/pdf";
            request.file(new MockMultipartFile("file", "cv.pdf", type, content));
        }
        if (scenario.equals("too-large")) expected = 413;
        if (scenario.equals("wrong-type")) expected = 415;
        mockMvc.perform(request)
                .andExpect(status().is(expected))
                .andExpect(jsonPath("$.status").value(expected));
    }

    @Test
    void upload_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(multipart("/me/cv").file(pdf()).with(request -> {
            request.setMethod("PUT");
            return request;
        })).andExpect(status().isUnauthorized());
    }

    @Test
    void upload_shouldUseOnlyTheAuthenticatedOwner() throws Exception {
        mockMvc.perform(multipart("/me/cv").file(pdf()).with(request -> {
            request.setMethod("PUT");
            return request;
        }).header("Authorization", "Bearer " + token)).andExpect(status().isCreated());
        User bob = new User();
        bob.setEmail("cv-controller-bob@example.com");
        bob.setPassword("unused");
        bob.setRole(Role.USER);
        String bobToken = jwtService.generateToken(userRepository.saveAndFlush(bob));
        mockMvc.perform(multipart("/me/cv").file(pdf()).param("userId", "-1").with(request -> {
            request.setMethod("PUT");
            return request;
        }).header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value(1));
        mockMvc.perform(multipart("/me/cv").file(pdf()).with(request -> {
            request.setMethod("PUT");
            return request;
        }).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void upload_shouldRejectUnsupportedRequestContentType() throws Exception {
        mockMvc.perform(put("/me/cv").contentType("application/pdf").content(new byte[]{1})
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }

    @Test
    void getMetadata_shouldReturnTheCurrentCvMetadataOnly() throws Exception {
        mockMvc.perform(multipart("/me/cv").file(pdf()).with(request -> {
            request.setMethod("PUT");
            return request;
        }).header("Authorization", "Bearer " + token)).andExpect(status().isCreated());
        var replacement = mockMvc.perform(multipart("/me/cv").file(pdf()).with(request -> {
            request.setMethod("PUT");
            return request;
        }).header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andReturn();

        mockMvc.perform(get("/me/cv").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().json(replacement.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.content").doesNotExist())
                .andExpect(jsonPath("$.text").doesNotExist());
    }

    @Test
    void getMetadata_shouldReturnNotFoundWhenThereIsNoCv() throws Exception {
        mockMvc.perform(get("/me/cv").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource not found"))
                .andExpect(jsonPath("$.message").value("CV not found"));
    }

    @Test
    void getMetadata_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/me/cv")).andExpect(status().isUnauthorized());
    }

    @Test
    void getMetadata_shouldNotReturnAnotherUsersCv() throws Exception {
        mockMvc.perform(multipart("/me/cv").file(pdf()).with(request -> {
            request.setMethod("PUT");
            return request;
        }).header("Authorization", "Bearer " + token)).andExpect(status().isCreated());
        User bob = new User();
        bob.setEmail("cv-metadata-bob@example.com");
        bob.setPassword("unused");
        bob.setRole(Role.USER);
        String bobToken = jwtService.generateToken(userRepository.saveAndFlush(bob));

        mockMvc.perform(get("/me/cv").param("userId",
                        userRepository.findByEmail("cv-controller@example.com").orElseThrow().getId().toString())
                .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void otherOperations_shouldReturnNotImplemented() throws Exception {
        mockMvc.perform(get("/me/cv/file").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotImplemented());
        mockMvc.perform(delete("/me/cv").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotImplemented());
    }
}
