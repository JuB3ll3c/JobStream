package com.jobstream.api.service;

import com.jobstream.api.config.TestContainerConfig;
import com.jobstream.api.entity.Job;
import com.jobstream.api.entity.Role;
import com.jobstream.api.entity.User;
import com.jobstream.api.exception.ResourceNotFoundException;
import com.jobstream.api.exception.ResourceConflictException;
import com.jobstream.api.repository.JobRepository;
import com.jobstream.api.repository.UserRepository;
import com.jobstream.dto.JobRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
@Transactional
class JobServiceTest {
    @Autowired
    private JobService jobService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

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

    private Job createJob(String externalId, User owner) {
        Job job = new Job();
        job.setExternalId(externalId);
        job.setTitle("Java Developer");
        job.setCompany("Acme");
        job.setLocation("Zurich");
        job.setUser(owner);
        return jobRepository.saveAndFlush(job);
    }

    @Test
    void getJobById_shouldHideAnotherUsersOffer() {
        Job bobsOffer = createJob("external-1", bob);

        assertThatThrownBy(() -> jobService.getJobById(bobsOffer.getId(), alice.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void saveJob_shouldAttachOfferToSuppliedOwner() {
        var saved = jobService.saveJob(alice.getId(), new JobRequestDto("external-1", "Java Developer", "Acme", "Zurich"));

        assertThat(jobService.getJobById(saved.getId(), alice.getId()).getExternalId()).isEqualTo("external-1");
        assertThatThrownBy(() -> jobService.getJobById(saved.getId(), bob.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getJobs_shouldPaginateOnlySuppliedOwnersOffers() {
        createJob("alice-1", alice);
        createJob("alice-2", alice);
        createJob("alice-3", alice);
        createJob("bob-1", bob);

        var page = jobService.getJobs(alice.getId(), PageRequest.of(0, 2, Sort.by("externalId")));
        assertThat(page.getContent()).extracting(job -> job.getExternalId())
                .containsExactly("alice-1", "alice-2");
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void deleteJob_shouldHideAndPreserveAnotherUsersOffer() {
        Job bobsOffer = createJob("external-1", bob);

        assertThatThrownBy(() -> jobService.deleteJob(bobsOffer.getId(), alice.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(jobService.getJobById(bobsOffer.getId(), bob.getId()).getExternalId()).isEqualTo("external-1");
    }

    @Test
    void saveJob_shouldAllowSameExternalOfferForDifferentUsers() {
        var request = new JobRequestDto("external-1", "Java Developer", "Acme", "Zurich");
        var alicesOffer = jobService.saveJob(alice.getId(), request);
        var bobsOffer = jobService.saveJob(bob.getId(), request);

        assertThat(bobsOffer.getId()).isNotEqualTo(alicesOffer.getId());
        assertThat(jobService.getJobById(bobsOffer.getId(), bob.getId()).getExternalId()).isEqualTo("external-1");
        assertThatThrownBy(() -> jobService.getJobById(alicesOffer.getId(), bob.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void saveJob_shouldRejectDuplicateWithinUsersCollection() {
        var request = new JobRequestDto("external-1", "Java Developer", "Acme", "Zurich");
        jobService.saveJob(alice.getId(), request);

        assertThatThrownBy(() -> jobService.saveJob(alice.getId(), request))
                .isInstanceOf(ResourceConflictException.class);
        assertThat(jobService.getJobs(alice.getId(), PageRequest.of(0, 20)).getTotalElements()).isEqualTo(1);
    }

    @Test
    void deleteJob_shouldRemoveOwnersOffer() {
        var saved = jobService.saveJob(alice.getId(), new JobRequestDto("external-1", "Java Developer", "Acme", "Zurich"));
        jobService.deleteJob(saved.getId(), alice.getId());

        assertThatThrownBy(() -> jobService.getJobById(saved.getId(), alice.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(jobService.getJobs(alice.getId(), PageRequest.of(0, 20)).getTotalElements()).isZero();
    }

    @Test
    void missingOffer_shouldBeInvisibleForReadingAndDeletion() {
        assertThatThrownBy(() -> jobService.getJobById(Long.MAX_VALUE, alice.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> jobService.deleteJob(Long.MAX_VALUE, alice.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getJobs_shouldReturnEmptyPersonalCollectionEvenWhenOthersHaveOffers() {
        createJob("bob-1", bob);
        var page = jobService.getJobs(alice.getId(), PageRequest.of(0, 20));
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getTotalPages()).isZero();
    }
}
