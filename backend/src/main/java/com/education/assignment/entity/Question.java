package com.education.assignment.entity;

import com.education.assignment.enums.QuestionType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(nullable = false)
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(length = 2000, nullable = false)
    private String content;

    @Column(length = 1000)
    private String options;

    @Column(length = 1000)
    private String correctAnswer;

    @Column(nullable = false)
    private Integer score;

    private String knowledgePoints;

    private String difficulty;

    private String explanation;

    @Column(nullable = false)
    private Boolean autoGradable = false;

    @OneToMany(mappedBy = "question")
    private List<SubmissionAnswer> answers = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
