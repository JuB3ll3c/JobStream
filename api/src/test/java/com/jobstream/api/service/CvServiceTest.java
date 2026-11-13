package com.jobstream.api.service;

import com.jobstream.api.config.TestContainerConfig;
import com.jobstream.api.entity.Role;
import com.jobstream.api.exception.CvValidationException;
import com.jobstream.api.exception.ResourceNotFoundException;
import com.jobstream.api.entity.User;
import com.jobstream.api.repository.UserCvRepository;
import com.jobstream.api.repository.UserRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
class CvServiceTest {
    @Autowired
    private CvService cvService;

    @Autowired
    private UserCvRepository cvRepository;

    @Autowired
    private UserRepository userRepository;

    private User alice;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("cv-service-alice@example.com");
        user.setPassword("unused");
        user.setRole(Role.USER);
        alice = userRepository.saveAndFlush(user);
    }

    @AfterEach
    void cleanUp() {
        if (alice != null) {
            cvRepository.findByUserId(alice.getId()).ifPresent(cvRepository::delete);
            userRepository.delete(alice);
        }
    }

    private static byte[] pdf(String title) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.getDocumentInformation().setTitle(title);
            document.save(output);
            return output.toByteArray();
        }
    }

    @Test
    void delete_shouldRemoveTheCommittedCvAndItsMetadata() throws IOException {
        cvService.uploadCv(alice.getId(),
                new MockMultipartFile("file", "cv.pdf", "application/pdf", pdf("CV")));

        cvService.deleteCv(alice.getId());

        assertThatThrownBy(() -> cvService.getCvFile(alice.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> cvService.getCvMetadata(alice.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(cvRepository.findByUserId(alice.getId())).isEmpty();
        assertThat(userRepository.existsById(alice.getId())).isTrue();
    }

    @Test
    void delete_shouldThrowNotFoundWhenThereIsNoCv() {
        assertThatThrownBy(() -> cvService.deleteCv(alice.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getFile_shouldReturnTheCurrentPdfBytesAndFilename() throws IOException {
        cvService.uploadCv(alice.getId(),
                new MockMultipartFile("file", "first.pdf", "application/pdf", pdf("First")));
        byte[] current = pdf("Current");
        cvService.uploadCv(alice.getId(),
                new MockMultipartFile("file", "current.pdf", "application/pdf", current));

        var file = cvService.getCvFile(alice.getId());

        assertThat(file.getContent()).containsExactly(current);
        assertThat(file.getOriginalFilename()).isEqualTo("current.pdf");
    }

    @Test
    void getFile_shouldThrowNotFoundWhenThereIsNoCv() {
        assertThatThrownBy(() -> cvService.getCvFile(alice.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMetadata_shouldReturnTheCurrentCvMetadata() throws IOException {
        cvService.uploadCv(alice.getId(),
                new MockMultipartFile("file", "first.pdf", "application/pdf", pdf("First")));
        var replacement = cvService.uploadCv(alice.getId(),
                new MockMultipartFile("file", "current.pdf", "application/pdf", pdf("Current")));

        var metadata = cvService.getCvMetadata(alice.getId());

        assertThat(metadata).isEqualTo(replacement.metadata());
    }

    @Test
    void getMetadata_shouldThrowNotFoundWhenThereIsNoCv() {
        assertThatThrownBy(() -> cvService.getCvMetadata(alice.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void upload_shouldCreateTheOwnersFirstCvAndReturnItsMetadata() throws IOException {
        byte[] content = pdf("Original CV");
        var result = cvService.uploadCv(alice.getId(),
                new MockMultipartFile("file", "cv.pdf", "application/pdf", content));

        assertThat(result.created()).isTrue();
        assertThat(result.metadata().getOriginalFilename()).isEqualTo("cv.pdf");
        assertThat(result.metadata().getSizeBytes()).isEqualTo((long) content.length);
        assertThat(result.metadata().getUploadedAt()).isNotNull();
        assertThat(result.metadata().getVersion()).isEqualTo(1L);
        var stored = cvRepository.findByUserId(alice.getId()).orElseThrow();
        assertThat(stored.getContent()).containsExactly(content);
        assertThat(stored.getUploadedAt()).isEqualTo(result.metadata().getUploadedAt());
        assertThat(stored.getRevision()).isEqualTo(1L);
        assertThat(stored.getVersion()).isZero();
    }
    @Test
    void upload_shouldReplaceTheOwnersCvAndIncrementItsRevision() throws IOException {
        var first = cvService.uploadCv(alice.getId(),
                new MockMultipartFile("file", "first.pdf", "application/pdf", pdf("First")));
        Long originalId = cvRepository.findByUserId(alice.getId()).orElseThrow().getId();
        byte[] replacement = pdf("Replacement");

        var result = cvService.uploadCv(alice.getId(),
                new MockMultipartFile("file", "replacement.pdf", "application/pdf", replacement));

        assertThat(result.created()).isFalse();
        assertThat(result.metadata().getVersion()).isEqualTo(2L);
        assertThat(result.metadata().getOriginalFilename()).isEqualTo("replacement.pdf");
        assertThat(result.metadata().getSizeBytes()).isEqualTo((long) replacement.length);
        assertThat(result.metadata().getUploadedAt()).isAfterOrEqualTo(first.metadata().getUploadedAt());
        var stored = cvRepository.findByUserId(alice.getId()).orElseThrow();
        assertThat(stored.getId()).isEqualTo(originalId);
        assertThat(stored.getContent()).containsExactly(replacement);
        assertThat(stored.getRevision()).isEqualTo(2L);
        assertThat(stored.getVersion()).isEqualTo(1L);
    }

    @Test
    void upload_shouldRejectDisguisedNonPdfAndPreserveTheCommittedCv() throws IOException {
        byte[] original = pdf("Original");
        var first = cvService.uploadCv(alice.getId(),
                new MockMultipartFile("file", "original.pdf", "application/pdf", original));

        assertThatThrownBy(() -> cvService.uploadCv(alice.getId(),
                new MockMultipartFile("file", "disguised.pdf", "application/pdf", new byte[]{1, 2, 3})))
                .isInstanceOf(CvValidationException.class)
                .extracting(error -> ((CvValidationException) error).getReason())
                .isEqualTo(CvValidationException.Reason.INVALID_FILE);

        var stored = cvRepository.findByUserId(alice.getId()).orElseThrow();
        assertThat(stored.getContent()).containsExactly(original);
        assertThat(stored.getOriginalFilename()).isEqualTo("original.pdf");
        assertThat(stored.getRevision()).isEqualTo(1L);
        assertThat(stored.getVersion()).isZero();
        assertThat(stored.getUploadedAt()).isEqualTo(first.metadata().getUploadedAt());
    }

    @Test
    void upload_shouldKeepEachOwnersCvSeparate() throws IOException {
        User bob = new User();
        bob.setEmail("cv-service-bob@example.com");
        bob.setPassword("unused");
        bob.setRole(Role.USER);
        bob = userRepository.saveAndFlush(bob);
        try {
            byte[] bobContent = pdf("Bob");
            cvService.uploadCv(bob.getId(),
                    new MockMultipartFile("file", "bob.pdf", "application/pdf", bobContent));
            cvService.uploadCv(alice.getId(),
                    new MockMultipartFile("file", "alice.pdf", "application/pdf", pdf("Alice")));
            cvService.uploadCv(alice.getId(),
                    new MockMultipartFile("file", "alice-new.pdf", "application/pdf", pdf("Alice updated")));

            var stored = cvRepository.findByUserId(bob.getId()).orElseThrow();
            assertThat(stored.getContent()).containsExactly(bobContent);
            assertThat(stored.getOriginalFilename()).isEqualTo("bob.pdf");
            assertThat(stored.getRevision()).isEqualTo(1L);
        } finally {
            cvRepository.findByUserId(bob.getId()).ifPresent(cvRepository::delete);
            userRepository.delete(bob);
        }
    }

    @Test
    void upload_shouldAcceptExactlyFiveMegabytes() throws IOException {
        byte[] small = pdf("At limit");
        int trailer = new String(small, StandardCharsets.ISO_8859_1).lastIndexOf("startxref");
        byte[] content = new byte[5_000_000];
        System.arraycopy(small, 0, content, 0, trailer);
        int tailLength = small.length - trailer;
        Arrays.fill(content, trailer, content.length - tailLength, (byte) ' ');
        System.arraycopy(small, trailer, content, content.length - tailLength, tailLength);

        var result = cvService.uploadCv(alice.getId(),
                new MockMultipartFile("file", "cv.pdf", "application/pdf", content));

        assertThat(result.metadata().getSizeBytes()).isEqualTo(5_000_000L);
        assertThat(cvRepository.findByUserId(alice.getId()).orElseThrow().getContent().length)
                .isEqualTo(5_000_000);
    }

    private static Stream<Arguments> invalidFiles() throws IOException {
        byte[] valid = pdf("Valid");
        byte[] oversized = new byte[5_000_001];
        MultipartFile understated = new MockMultipartFile("file", "cv.pdf", "application/pdf", oversized) {
            @Override
            public long getSize() {
                return 1;
            }
        };
        return Stream.of(
                Arguments.of("missing", null, "INVALID_FILE"),
                Arguments.of("empty", new MockMultipartFile("file", "cv.pdf", "application/pdf", new byte[0]), "INVALID_FILE"),
                Arguments.of("truncated PDF", new MockMultipartFile("file", "cv.pdf", "application/pdf", "%PDF-1.7\n%%EOF".getBytes()), "INVALID_FILE"),
                Arguments.of("wrong media type", new MockMultipartFile("file", "cv.pdf", "image/png", valid), "UNSUPPORTED_MEDIA_TYPE"),
                Arguments.of("missing media type", new MockMultipartFile("file", "cv.pdf", null, valid), "UNSUPPORTED_MEDIA_TYPE"),
                Arguments.of("oversized", new MockMultipartFile("file", "cv.pdf", "application/pdf", oversized), "TOO_LARGE"),
                Arguments.of("understated size", understated, "TOO_LARGE"),
                Arguments.of("missing filename", new MockMultipartFile("file", null, "application/pdf", valid), "INVALID_FILE"),
                Arguments.of("blank filename", new MockMultipartFile("file", "   ", "application/pdf", valid), "INVALID_FILE"),
                Arguments.of("directory filename", new MockMultipartFile("file", "folder/", "application/pdf", valid), "INVALID_FILE"),
                Arguments.of("control in filename", new MockMultipartFile("file", "bad\nname.pdf", "application/pdf", valid), "INVALID_FILE"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidFiles")
    void upload_shouldRejectInvalidFilesWithoutChangingTheCommittedCv(
            String description, MultipartFile file, String reason) throws IOException {
        byte[] original = pdf("Original");
        var first = cvService.uploadCv(alice.getId(),
                new MockMultipartFile("file", "original.pdf", "application/pdf", original));

        assertThatThrownBy(() -> cvService.uploadCv(alice.getId(), file))
                .isInstanceOfSatisfying(CvValidationException.class,
                        error -> assertThat(error.getReason().name()).isEqualTo(reason));

        var stored = cvRepository.findByUserId(alice.getId()).orElseThrow();
        assertThat(stored.getContent()).containsExactly(original);
        assertThat(stored.getOriginalFilename()).isEqualTo("original.pdf");
        assertThat(stored.getRevision()).isEqualTo(1L);
        assertThat(stored.getVersion()).isZero();
        assertThat(stored.getUploadedAt()).isEqualTo(first.metadata().getUploadedAt());
    }

}
