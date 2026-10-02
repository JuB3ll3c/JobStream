package com.jobstream.api.repository;

import com.jobstream.api.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobRepository extends JpaRepository<Job, Long> {

    Optional<Job> findByIdAndUserId(Long id, Long userId);

    Page<Job> findAllByUserId(Long userId, Pageable pageable);

    boolean existsByExternalIdAndUserId(String externalId, Long userId);
}
