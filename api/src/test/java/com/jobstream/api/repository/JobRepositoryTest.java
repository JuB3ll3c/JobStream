package com.jobstream.api.repository;

import com.jobstream.api.config.TestContainerConfig;
import com.jobstream.api.entity.Job;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
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

    private Job createJob(String externalId) {
        Job job = new Job();
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
    void save_shouldPersistAndPopulateAuditFields() {
        Job saved = jobRepository.save(createJob("job_audit"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getVersion()).isEqualTo(0);
    }

    @Test
    void existsByExternalId_shouldReturnTrueWhenPresent() {
        jobRepository.save(createJob("job_exists"));

        assertThat(jobRepository.existsByExternalId("job_exists")).isTrue();
        assertThat(jobRepository.existsByExternalId("job_unknown")).isFalse();
    }

    @Test
    void save_shouldThrowOnDuplicateExternalId() {
        jobRepository.saveAndFlush(createJob("job_dup"));

        assertThatThrownBy(() -> jobRepository.saveAndFlush(createJob("job_dup")))
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