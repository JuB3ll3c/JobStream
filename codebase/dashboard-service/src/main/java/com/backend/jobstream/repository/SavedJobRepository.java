package com.backend.jobstream.repository;

import com.backend.jobstream.entity.SavedJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour les jobs sauvegardés
 */
@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, UUID> {

    /**
     * Vérifie si un job est déjà sauvegardé par son ID externe
     */
    boolean existsByExternalId(String externalId);

    /**
     * Trouve un job sauvegardé par son ID externe
     */
    Optional<SavedJob> findByExternalId(String externalId);
    
    @Query(value = "SELECT s FROM SavedJob s LEFT JOIN FETCH s.analysis", countQuery = "SELECT count(s) FROM SavedJob s")
    Page<SavedJob> findAllWithAnalysis(Pageable pageable);

    Page<SavedJob> findAllByOrderBySavedAtDesc(Pageable pageable);

    /**
     * Trouve les jobs sauvegardés par liste d'IDs externes
     */
    List<SavedJob> findByExternalIdIn(List<String> externalIds);
}
