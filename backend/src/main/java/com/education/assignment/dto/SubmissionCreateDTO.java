package com.education.assignment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubmissionCreateDTO {
    @NotNull(message = "作业ID不能为空")
    private Long assignmentId;
    
    private List<AnswerDTO> answers;
    
    @Data
    public static class AnswerDTO {
        private Long questionId;
        private String studentAnswer;
    }
}
