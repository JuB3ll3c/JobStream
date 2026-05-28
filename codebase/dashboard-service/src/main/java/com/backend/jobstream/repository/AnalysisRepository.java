package com.backend.jobstream.repository;

import com.backend.jobstream.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {
    
    Optional<Analysis> findBySavedJob_Id(UUID savedJobId);
}