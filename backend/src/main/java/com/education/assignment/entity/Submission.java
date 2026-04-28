package com.education.assignment.entity;

import com.education.assignment.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "submissions")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.SUBMITTED;

    @Column(nullable = false)
    private Integer versionNumber = 1;

    private LocalDateTime submittedAt;

    private LocalDateTime autoGradedAt;

    private LocalDateTime manuallyGradedAt;

    private LocalDateTime returnedAt;

    private Integer autoScore;

    private Integer manualScore;

    private Integer totalScore;

    private Integer objectiveScore;
    private Integer objectiveMaxScore;
    private Integer subjectiveScore;
    private Integer subjectiveMaxScore;

    private Double objectiveAccuracy;
    private Double subjectiveScoreRate;

    private Integer totalObjectiveQuestions;
    private Integer correctObjectiveQuestions;
    private Integer wrongObjectiveQuestions;
    private Integer totalSubjectiveQuestions;
    private Integer gradedSubjectiveQuestions;

    @Column(length = 1000)
    private String teacherComments;

    private Boolean isLate = false;

    @Column(length = 500)
    private String versionNote;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubmissionAnswer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL)
    @OrderBy("versionNumber DESC")
    private List<SubmissionVersion> versions = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (versionNumber == null) {
            versionNumber = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
