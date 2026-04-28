package com.education.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssignmentCreateDTO {
    @NotBlank(message = "作业标题不能为空")
    private String title;
    
    private String description;
    
    @NotNull(message = "班级ID不能为空")
    private Long classId;
    
    @NotNull(message = "截止时间不能为空")
    private LocalDateTime deadline;
    
    @NotNull(message = "总分不能为空")
    @Positive(message = "总分必须为正数")
    private Integer totalScore;
    
    private List<QuestionCreateDTO> questions;
    
    @Data
    public static class QuestionCreateDTO {
        private Integer orderIndex;
        private String type;
        private String content;
        private String options;
        private String correctAnswer;
        private Integer score;
        private String knowledgePoints;
        private String difficulty;
        private String explanation;
        private Boolean autoGradable;
    }
}
