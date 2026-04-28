package com.education.assignment.entity;

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

    @Column(length = 2000)
    private String studentAnswer;

    private Integer autoScore;
    private Integer manualScore;

    @Column(length = 1000)
    private String teacherFeedback;

    private Boolean isCorrect;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    private Integer questionScore;

    private Boolean autoGradable;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
