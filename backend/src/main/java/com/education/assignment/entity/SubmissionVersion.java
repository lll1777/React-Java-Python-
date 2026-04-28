package com.education.assignment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "submission_versions")
public class SubmissionVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(length = 500)
    private String versionNote;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime autoGradedAt;
    private LocalDateTime manuallyGradedAt;

    private Integer autoScore;
    private Integer manualScore;
    private Integer totalScore;

    private Integer objectiveScore;
    private Integer objectiveMaxScore;
    private Integer subjectiveScore;
    private Integer subjectiveMaxScore;

    private Double objectiveAccuracy;
    private Double subjectiveScoreRate;

    @Column(length = 2000)
    private String teacherComments;

    @OneToMany(mappedBy = "submissionVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubmissionAnswerVersion> answers = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isLatest = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }
}
