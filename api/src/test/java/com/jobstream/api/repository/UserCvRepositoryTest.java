package com.jobstream.api.repository;

import com.jobstream.api.config.TestContainerConfig;
import com.jobstream.api.entity.Role;
import com.jobstream.api.entity.User;
import com.jobstream.api.entity.UserCv;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
@Transactional
class UserCvRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserCvRepository userCvRepository;

    @Autowired
    private EntityManager entityManager;

    private User alice;

    @BeforeEach
    void setUp() {
        alice = createUser("cv-alice@example.com");
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("unused");
        user.setRole(Role.USER);
        return userRepository.saveAndFlush(user);
    }

    @Test
    void schema_shouldRejectASecondCvForTheSameOwner() {
        insertCv(alice.getId());

        assertThatThrownBy(() -> insertCv(alice.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_shouldRoundTripBinaryPdfAndMetadata() {
        byte[] pdf = ("%PDF-1.7\n" + "CV text\n".repeat(1024)).getBytes(StandardCharsets.UTF_8);
        pdf[pdf.length - 2] = 0;
        pdf[pdf.length - 1] = (byte) 0xff;
        UserCv cv = newCv(alice, pdf);
        cv.setOriginalFilename("CV – Développeur.pdf");
        Long id = userCvRepository.saveAndFlush(cv).getId();
        entityManager.clear();

        UserCv loaded = userCvRepository.findByUserId(alice.getId()).orElseThrow();
        assertThat(loaded.getId()).isEqualTo(id);
        assertThat(loaded.getContent()).containsExactly(pdf);
        assertThat(loaded.getUser().getId()).isEqualTo(alice.getId());
        assertThat(loaded.getOriginalFilename()).isEqualTo("CV – Développeur.pdf");
        assertThat(loaded.getSizeBytes()).isEqualTo((long) pdf.length);
        assertThat(loaded.getUploadedAt()).isEqualTo(LocalDateTime.of(2026, 11, 11, 10, 0));
        assertThat(loaded.getRevision()).isEqualTo(1L);
        assertThat(loaded.getVersion()).isZero();
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getUpdatedAt()).isNotNull();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT pg_typeof(content)::text FROM user_cv WHERE id = ?", String.class, id))
                .isEqualTo("bytea");
    }

    @Test
    void findByUserId_shouldKeepEachOwnersCvSeparate() {
        User bob = createUser("cv-bob@example.com");
        assertThat(userCvRepository.findByUserId(bob.getId())).isEmpty();
        Long alicesCvId = userCvRepository.saveAndFlush(newCv(alice, new byte[]{1, 2, 3})).getId();
        Long bobsCvId = userCvRepository.saveAndFlush(newCv(bob, new byte[]{4, 5, 6})).getId();
        entityManager.clear();

        UserCv alicesCv = userCvRepository.findByUserId(alice.getId()).orElseThrow();
        UserCv bobsCv = userCvRepository.findByUserId(bob.getId()).orElseThrow();
        assertThat(alicesCv.getId()).isEqualTo(alicesCvId);
        assertThat(alicesCv.getContent()).containsExactly((byte) 1, (byte) 2, (byte) 3);
        assertThat(bobsCv.getId()).isEqualTo(bobsCvId).isNotEqualTo(alicesCvId);
        assertThat(bobsCv.getContent()).containsExactly((byte) 4, (byte) 5, (byte) 6);
    }

    @Test
    void updates_shouldKeepDocumentRevisionSeparateFromOptimisticLockVersion() {
        UserCv cv = newCv(alice, new byte[]{1, 2, 3});
        cv.setRevision(7L);
        userCvRepository.saveAndFlush(cv);
        assertThat(cv.getVersion()).isZero();
        cv.setOriginalFilename("renamed.pdf");
        userCvRepository.saveAndFlush(cv);
        entityManager.clear();

        UserCv loaded = userCvRepository.findByUserId(alice.getId()).orElseThrow();
        assertThat(loaded.getVersion()).isEqualTo(1L);
        assertThat(loaded.getRevision()).isEqualTo(7L);
        assertThat(loaded.getUploadedAt()).isEqualTo(LocalDateTime.of(2026, 11, 11, 10, 0));
    }

    @Test
    void save_shouldRejectACvWithoutOwner() {
        UserCv cv = newCv(null, new byte[]{1, 2, 3});

        assertThatThrownBy(() -> userCvRepository.saveAndFlush(cv))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_shouldRejectAnUnknownOwner() {
        User unknownOwner = userRepository.getReferenceById(Long.MAX_VALUE);
        UserCv cv = newCv(unknownOwner, new byte[]{1, 2, 3});

        assertThatThrownBy(() -> userCvRepository.saveAndFlush(cv))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_shouldRejectAnOutdatedDetachedCv() {
        userCvRepository.saveAndFlush(newCv(alice, new byte[]{1, 2, 3}));
        entityManager.clear();
        UserCv outdated = userCvRepository.findByUserId(alice.getId()).orElseThrow();
        entityManager.detach(outdated);
        UserCv current = userCvRepository.findByUserId(alice.getId()).orElseThrow();
        current.setOriginalFilename("current.pdf");
        userCvRepository.saveAndFlush(current);
        entityManager.clear();

        outdated.setOriginalFilename("outdated.pdf");
        assertThatThrownBy(() -> userCvRepository.saveAndFlush(outdated))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    private UserCv newCv(User owner, byte[] content) {
        UserCv cv = new UserCv();
        cv.setUser(owner);
        cv.setContent(content);
        cv.setOriginalFilename("cv.pdf");
        cv.setSizeBytes((long) content.length);
        cv.setUploadedAt(LocalDateTime.of(2026, 11, 11, 10, 0));
        cv.setRevision(1L);
        return cv;
    }

    private void insertCv(Long userId) {
        jdbcTemplate.update("""
                INSERT INTO user_cv (user_id, content, original_filename, size_bytes, uploaded_at, revision)
                VALUES (?, ?, ?, ?, ?, ?)
                """, userId, new byte[]{37, 80, 68, 70}, "cv.pdf", 4L,
                LocalDateTime.of(2026, 11, 11, 10, 0), 1L);
    }
}
