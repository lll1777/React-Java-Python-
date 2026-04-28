package com.education.assignment.entity;

import com.education.assignment.enums.QuestionType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "submission_answer_versions")
public class SubmissionAnswerVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_version_id", nullable = false)
    private SubmissionVersion submissionVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType;

    @Column(nullable = false)
    private Integer questionOrderIndex;

    @Column(nullable = false)
    private Integer questionScore;

    private String questionCorrectAnswer;

    private Boolean questionAutoGradable;

    @Column(length = 2000)
    private String studentAnswer;

    private Integer autoScore;

    private Integer manualScore;

    @Column(nullable = false)
    private Integer effectiveScore = 0;

    private Boolean isCorrect;

    @Column(length = 1000)
    private String teacherFeedback;

    @Column(length = 500)
    private String knowledgePoints;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (effectiveScore == null) {
            effectiveScore = 0;
        }
    }
}
