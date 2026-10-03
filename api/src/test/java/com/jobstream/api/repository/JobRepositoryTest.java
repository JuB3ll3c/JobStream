package com.jobstream.api.repository;

import com.jobstream.api.config.TestContainerConfig;
import com.jobstream.api.entity.Job;
import com.jobstream.api.entity.User;
import com.jobstream.api.entity.Role;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
@Transactional
class JobRepositoryTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = createUser("alice@example.com");
        bob = createUser("bob@example.com");
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("unused");
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    private Job createJob(String externalId) {
        Job job = new Job();
        job.setUser(alice);
        job.setExternalId(externalId);
        job.setTitle("Java Developer");
        job.setCompany("TechCorp");
        job.setLocation("Paris");
        job.setDescription("Description");
        job.setSalaryMin(50000);
        job.setSalaryMax(80000);
        job.setContractType("CDI");
        job.setPostedDate(LocalDate.of(2026, 3, 25));
        job.setJobUrl("https://example.com/job/" + externalId);
        job.setRequirements(List.of("Java 17", "Spring Boot"));
        return job;
    }

    @Test
    void save_shouldRoundTripLongDescriptionAndUrl() {
        Job job = createJob("long_offer");
        String description = "Detailed job description. ".repeat(200);
        String url = "https://example.com/jobs?tracking=" + "a".repeat(1000);
        job.setDescription(description);
        job.setJobUrl(url);

        Long id = jobRepository.saveAndFlush(job).getId();
        entityManager.clear();

        Job loaded = jobRepository.findById(id).orElseThrow();
        assertThat(loaded.getDescription()).isEqualTo(description);
        assertThat(loaded.getJobUrl()).isEqualTo(url);
    }

    @Test
    void save_shouldRoundTripContractTypeOf255Characters() {
        Job job = createJob("long_contract_type");
        String contractType = "C".repeat(255);
        job.setContractType(contractType);

        Long id = jobRepository.saveAndFlush(job).getId();
        entityManager.clear();

        Job loaded = jobRepository.findById(id).orElseThrow();
        assertThat(loaded.getContractType()).isEqualTo(contractType);
    }

    @Test
    void save_shouldPersistAndPopulateAuditFields() {
        Job saved = jobRepository.save(createJob("job_audit"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getVersion()).isEqualTo(0);
    }

    @Test
    void existsByExternalIdAndUserId_shouldCheckOnlyOwnersCollection() {
        jobRepository.save(createJob("job_exists"));

        assertThat(jobRepository.existsByExternalIdAndUserId("job_exists", alice.getId())).isTrue();
        assertThat(jobRepository.existsByExternalIdAndUserId("job_exists", bob.getId())).isFalse();
        assertThat(jobRepository.existsByExternalIdAndUserId("job_unknown", alice.getId())).isFalse();
    }

    @Test
    void save_shouldThrowOnDuplicateExternalId() {
        jobRepository.saveAndFlush(createJob("job_dup"));

        assertThatThrownBy(() -> jobRepository.saveAndFlush(createJob("job_dup")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_shouldAllowSameExternalIdForDifferentOwners() {
        jobRepository.saveAndFlush(createJob("shared_offer"));
        Job bobsOffer = createJob("shared_offer");
        bobsOffer.setUser(bob);

        Job saved = jobRepository.saveAndFlush(bobsOffer);

        assertThat(jobRepository.findByIdAndUserId(saved.getId(), bob.getId())).isPresent();
        assertThat(jobRepository.findByIdAndUserId(saved.getId(), alice.getId())).isEmpty();
    }

    @Test
    void save_shouldRejectMissingOwner() {
        Job orphan = createJob("orphan_offer");
        orphan.setUser(null);

        assertThatThrownBy(() -> jobRepository.saveAndFlush(orphan))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void initializeSqlSchema() {
        // This schema exists only inside this rolled-back Testcontainers transaction.
        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            try (var statement = connection.createStatement()) {
                statement.execute("CREATE SCHEMA saved_offer_contract");
                statement.execute("SET LOCAL search_path TO saved_offer_contract");
            }
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/migration/V1__initial_schema.sql"));
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("fixtures/data.sql"));
            return null;
        });
    }

    @Test
    void sqlSchema_shouldEnforceUniquenessPerUserAndLoadOwnedFixtures() {
        initializeSqlSchema();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM job WHERE user_id IS NULL", Long.class))
                .isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT user_id FROM job WHERE external_id = 'adzuna_007'", Long.class))
                .isEqualTo(2L);
        jdbcTemplate.update("INSERT INTO job (external_id, title, company, user_id) VALUES ('shared', 'Java', 'Acme', 2)");
        jdbcTemplate.update("INSERT INTO job (external_id, title, company, user_id) VALUES ('shared', 'Java', 'Acme', 3)");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM job WHERE external_id = 'shared'", Long.class))
                .isEqualTo(2);
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO job (external_id, title, company, user_id) VALUES ('shared', 'Java', 'Acme', 2)"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void sqlSchema_shouldRejectOffersWithoutOwner() {
        initializeSqlSchema();
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO job (external_id, title, company) VALUES ('orphan', 'Java', 'Acme')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void sqlSchema_shouldPreventDeletionFromLeavingOrphanOffers() {
        initializeSqlSchema();
        assertThatThrownBy(() -> jdbcTemplate.update("DELETE FROM app_user WHERE id = 2"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void requirements_shouldRoundTripJson() {
        Job saved = jobRepository.saveAndFlush(createJob("job_json"));

        Job loaded = jobRepository.findById(saved.getId()).orElseThrow();

        assertThat(loaded.getRequirements()).containsExactly("Java 17", "Spring Boot");
    }

    @Test
    void findAll_shouldSupportPagination() {
        for (int i = 0; i < 5; i++) {
            jobRepository.save(createJob("job_page_" + i));
        }

        Page<Job> page = jobRepository.findAll(PageRequest.of(0, 2));

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void update_shouldIncrementVersion() {
        Job saved = jobRepository.save(createJob("job_version"));

        saved.setTitle("Updated title");
        Job updated = jobRepository.saveAndFlush(saved);

        assertThat(updated.getVersion()).isEqualTo(1);
    }
}
