package com.education.assignment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class GradingDTO {
    private List<QuestionGradingDTO> questions;
    private String teacherComments;
    
    @Data
    public static class QuestionGradingDTO {
        private Long questionId;
        private Long submissionAnswerId;
        
        @Min(0)
        private Integer score;
        
        private String feedback;
    }
}
