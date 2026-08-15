package com.jobstream.api.repository;

import com.jobstream.api.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {

    boolean existsByExternalId(String externalId);
}
