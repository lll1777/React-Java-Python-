package com.education.assignment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wrong_questions")
public class WrongQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_answer_id")
    private SubmissionAnswer submissionAnswer;

    @Column(length = 2000)
    private String studentAnswer;

    private Integer wrongCount = 1;

    private Boolean isResolved = false;

    private LocalDateTime firstWrongAt;

    private LocalDateTime lastWrongAt;

    private LocalDateTime resolvedAt;

    @Column(length = 1000)
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (firstWrongAt == null) {
            firstWrongAt = LocalDateTime.now();
        }
        if (lastWrongAt == null) {
            lastWrongAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
