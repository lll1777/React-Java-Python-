package com.education.assignment.entity;

import com.education.assignment.enums.AssignmentStatus;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime autoGradedAt;
    private LocalDateTime manuallyGradedAt;
    private LocalDateTime returnedAt;

    @Column(nullable = false)
    private Integer autoScore = 0;

    @Column(nullable = false)
    private Integer manualScore = 0;

    @Column(nullable = false)
    private Integer totalScore = 0;

    @Column(nullable = false)
    private Integer objectiveScore = 0;

    @Column(nullable = false)
    private Integer objectiveMaxScore = 0;

    @Column(nullable = false)
    private Integer subjectiveScore = 0;

    @Column(nullable = false)
    private Integer subjectiveMaxScore = 0;

    @Column(nullable = false)
    private Integer assignmentTotalScore = 0;

    private Double objectiveAccuracy;
    private Double subjectiveScoreRate;
    private Double totalScoreRate;

    @Column(nullable = false)
    private Integer totalObjectiveQuestions = 0;

    @Column(nullable = false)
    private Integer correctObjectiveQuestions = 0;

    @Column(nullable = false)
    private Integer wrongObjectiveQuestions = 0;

    @Column(nullable = false)
    private Integer totalSubjectiveQuestions = 0;

    @Column(nullable = false)
    private Integer gradedSubjectiveQuestions = 0;

    @Column(nullable = false)
    private Integer totalQuestions = 0;

    @Column(nullable = false)
    private Integer answeredQuestions = 0;

    private Integer singleChoiceScore = 0;
    private Integer singleChoiceMaxScore = 0;
    private Integer singleChoiceCorrect = 0;
    private Integer singleChoiceTotal = 0;

    private Integer multipleChoiceScore = 0;
    private Integer multipleChoiceMaxScore = 0;
    private Integer multipleChoiceCorrect = 0;
    private Integer multipleChoiceTotal = 0;

    private Integer trueFalseScore = 0;
    private Integer trueFalseMaxScore = 0;
    private Integer trueFalseCorrect = 0;
    private Integer trueFalseTotal = 0;

    private Integer fillBlankScore = 0;
    private Integer fillBlankMaxScore = 0;
    private Integer fillBlankCorrect = 0;
    private Integer fillBlankTotal = 0;

    private Integer shortAnswerScore = 0;
    private Integer shortAnswerMaxScore = 0;
    private Integer shortAnswerGraded = 0;
    private Integer shortAnswerTotal = 0;

    private Integer essayScore = 0;
    private Integer essayMaxScore = 0;
    private Integer essayGraded = 0;
    private Integer essayTotal = 0;

    private Integer codingScore = 0;
    private Integer codingMaxScore = 0;
    private Integer codingGraded = 0;
    private Integer codingTotal = 0;

    @Column(length = 2000)
    private String teacherComments;

    private Boolean isLate = false;

    @OneToMany(mappedBy = "submissionVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubmissionAnswerVersion> answers = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isLatest = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (versionNumber == null) {
            versionNumber = 1;
        }
        if (autoScore == null) {
            autoScore = 0;
        }
        if (manualScore == null) {
            manualScore = 0;
        }
        if (totalScore == null) {
            totalScore = 0;
        }
        if (objectiveScore == null) {
            objectiveScore = 0;
        }
        if (objectiveMaxScore == null) {
            objectiveMaxScore = 0;
        }
        if (subjectiveScore == null) {
            subjectiveScore = 0;
        }
        if (subjectiveMaxScore == null) {
            subjectiveMaxScore = 0;
        }
        if (assignmentTotalScore == null) {
            assignmentTotalScore = 0;
        }
    }
}
