package com.education.assignment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "learning_reports")
public class LearningReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    private String title;

    @Column(length = 5000)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String detailData;

    private LocalDateTime reportPeriodStart;
    private LocalDateTime reportPeriodEnd;

    private Integer totalAssignments;
    private Integer submittedAssignments;
    private Integer gradedAssignments;

    private Double averageScore;
    private Double highestScore;
    private Double lowestScore;

    private Integer totalQuestions;
    private Integer correctQuestions;
    private Integer wrongQuestions;

    private Integer unresolvedWrongQuestions;
    private Integer resolvedWrongQuestions;

    private Integer totalObjectiveScore;
    private Integer totalObjectiveMaxScore;
    private Integer totalSubjectiveScore;
    private Integer totalSubjectiveMaxScore;
    private Integer totalObjectiveQuestions;
    private Integer totalSubjectiveQuestions;
    private Integer totalGradedSubjective;

    private Double averageObjectiveAccuracy;
    private Double overallObjectiveAccuracy;
    private Double averageSubjectiveScoreRate;

    private Double accuracyRate;

    @Column(columnDefinition = "TEXT")
    private String knowledgePointAnalysis;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    private LocalDateTime generatedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ReportType {
        DAILY,
        WEEKLY,
        MONTHLY,
        CUSTOM
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
