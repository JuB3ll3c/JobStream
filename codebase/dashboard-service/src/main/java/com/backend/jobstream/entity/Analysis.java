package com.backend.jobstream.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entité représentant une analyse IA d'un job sauvegardé
 */
@Entity
@Table(name = "analyses")
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_job_id", referencedColumnName = "id", unique = true)
    private SavedJob savedJob;

    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @ElementCollection
    @CollectionTable(name = "analysis_strengths", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "strength")
    private List<String> strengths;

    @ElementCollection
    @CollectionTable(name = "analysis_weaknesses", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "weakness")
    private List<String> weaknesses;

    @ElementCollection
    @CollectionTable(name = "analysis_recommendations", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "recommendation")
    private List<String> recommendations;

    @ElementCollection
    @CollectionTable(name = "analysis_tags", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message")
    private String errorMessage;

    public Analysis() {
    }

    public Analysis(UUID savedJobId) {
        this.savedJob = new SavedJob();
        this.savedJob.setId(savedJobId);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public SavedJob getSavedJob() { return savedJob; }
    public void setSavedJob(SavedJob savedJob) { this.savedJob = savedJob; }

    public UUID getSavedJobId() { 
        return savedJob != null ? savedJob.getId() : null; 
    }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }

    public List<String> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}