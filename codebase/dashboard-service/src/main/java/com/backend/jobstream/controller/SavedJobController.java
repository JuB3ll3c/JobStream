package com.backend.jobstream.controller;

import com.jobstream.dto.JobAnalysisResult;
import com.jobstream.dto.PagedSavedJobResponse;
import com.jobstream.dto.SaveJobRequest;
import com.jobstream.dto.SavedJobDto;
import com.backend.jobstream.entity.SavedJob;
import com.backend.jobstream.mapper.SavedJobMapper;
import com.backend.jobstream.repository.SavedJobRepository;
import com.backend.jobstream.service.AnalysisService;
import com.backend.jobstream.util.UuidUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller pour la gestion des jobs sauvegardés
 * Endpoints: GET/POST /api/saved-jobs
 */
@RestController
@RequestMapping("/api/saved-jobs")
@Validated
public class SavedJobController {

    private static final Logger log = LoggerFactory.getLogger(SavedJobController.class);

    private final SavedJobRepository savedJobRepository;
    private final SavedJobMapper savedJobMapper;
    private final AnalysisService analysisService;

    public SavedJobController(
            SavedJobRepository savedJobRepository,
            SavedJobMapper savedJobMapper,
            AnalysisService analysisService) {
        this.savedJobRepository = savedJobRepository;
        this.savedJobMapper = savedJobMapper;
        this.analysisService = analysisService;
    }

    /**
     * Sauvegarde un job
     */
    @PostMapping
    public ResponseEntity<SavedJobDto> saveJob(@Valid @RequestBody SaveJobRequest request) {
        log.info("Sauvegarde du job: externalId={}, title={}", request.getExternalId(), request.getTitle());

        // Vérifier si déjà sauvegardé
        if (savedJobRepository.existsByExternalId(request.getExternalId())) {
            log.warn("Job déjà sauvegardé: externalId={}", request.getExternalId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cette offre est déjà sauvegardée");
        }

        // Mapper request vers entity et sauvegarder
        SavedJob savedJob = savedJobMapper.toEntity(request);
        SavedJob saved = savedJobRepository.save(savedJob);
        
        log.info("Job sauvegardé avec succès: id={}, externalId={}", saved.getId(), saved.getExternalId());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedJobMapper.toDto(saved));
    }

    /**
     * Liste les jobs sauvegardés (paginé)
     */
    @GetMapping
    public ResponseEntity<PagedSavedJobResponse> getSavedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "savedAt,desc") String sort) {

        log.info("Récupération des jobs sauvegardés: page={}, size={}, sort={}", page, size, sort);

        // Parse sort parameter
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        Sort sortObj = Sort.by(direction, sortParams[0]);

        var pageable = PageRequest.of(page, size, sortObj);
        var savedJobsPage = savedJobRepository.findAllWithAnalysis(pageable);

        List<SavedJobDto> jobs = savedJobsPage.getContent().stream()
                .map(savedJobMapper::toDto)
                .toList();

        PagedSavedJobResponse response = new PagedSavedJobResponse();
        response.setContent(jobs);
        response.setPage(savedJobsPage.getNumber());
        response.setSize(savedJobsPage.getSize());
        response.setTotalElements((int) savedJobsPage.getTotalElements());
        response.setTotalPages(savedJobsPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * Vérifie quels jobs sont déjà sauvegardés (par liste d'externalIds)
     * Retourne la liste des IDs internes (UUID) des jobs sauvegardés
     */
    @PostMapping("/check")
    public ResponseEntity<List<UUID>> checkSavedJobs(
            @RequestBody @Size(max = 100) List<String> externalIds) {
        log.info("Vérification des jobs sauvegardés: {} IDs", externalIds.size());
        
        List<SavedJob> savedJobs = savedJobRepository.findByExternalIdIn(externalIds);
        List<UUID> savedIds = savedJobs.stream()
                .map(SavedJob::getId)
                .toList();
        
        log.info("Jobs sauvegardés trouvés: {}", savedIds.size());
        return ResponseEntity.ok(savedIds);
    }

    /**
     * Supprime un job sauvegardé
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSavedJob(@PathVariable String id) {
        log.info("Suppression du job sauvegardé: id={}", id);

        UUID uuid = UuidUtil.parseUuid(id);
        if (!savedJobRepository.existsById(uuid)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job sauvegardé non trouvé");
        }
        savedJobRepository.deleteById(uuid);
        return ResponseEntity.noContent().build();
    }

    /**
     * Déclenche l'analyse IA d'un job sauvegardé via Kafka
     */
    @PostMapping("/{id}/analyze")
    public ResponseEntity<Void> triggerAnalysis(@PathVariable String id) {
        analysisService.triggerAnalysis(id);
        return ResponseEntity.accepted().build();
    }

    /**
     * Récupère le résultat de l'analyse IA d'un job sauvegardé
     */
    @GetMapping("/{id}/analysis")
    public ResponseEntity<JobAnalysisResult> getAnalysis(@PathVariable String id) {
        log.info("Récupération de l'analyse pour le job sauvegardé: id={}", id);

        JobAnalysisResult result = analysisService.getAnalysis(id);
        return ResponseEntity.ok(result);
    }

}
