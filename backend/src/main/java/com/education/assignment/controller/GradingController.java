package com.education.assignment.controller;

import com.education.assignment.dto.GradingDTO;
import com.education.assignment.entity.Submission;
import com.education.assignment.service.GradingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/grading")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GradingController {
    private final GradingService gradingService;

    @PostMapping("/auto/start/{submissionId}")
    public ResponseEntity<Map<String, Object>> startAutoGrading(@PathVariable Long submissionId) {
        Submission submission = gradingService.startAutoGrading(submissionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("submissionId", submissionId);
        result.put("status", submission.getStatus());
        result.put("message", "开始自动批改");
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/auto/perform/{submissionId}")
    public ResponseEntity<GradingService.GradingResult> performAutoGrading(@PathVariable Long submissionId) {
        GradingService.GradingResult result = gradingService.performAutoGrading(submissionId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/manual/start/{submissionId}")
    public ResponseEntity<Map<String, Object>> startManualGrading(@PathVariable Long submissionId) {
        Submission submission = gradingService.startManualGrading(submissionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("submissionId", submissionId);
        result.put("status", submission.getStatus());
        result.put("message", "开始手工批改");
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/manual/perform/{submissionId}")
    public ResponseEntity<GradingService.GradingResult> performManualGrading(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradingDTO gradingDTO) {
        GradingService.GradingResult result = gradingService.performManualGrading(submissionId, gradingDTO);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/complete/{submissionId}")
    public ResponseEntity<GradingService.GradingResult> completeGrading(@PathVariable Long submissionId) {
        GradingService.GradingResult result = gradingService.completeGrading(submissionId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/python/auto-grade/{submissionId}")
    public ResponseEntity<GradingService.AutoGradeResult> callPythonAutoGrading(@PathVariable Long submissionId) {
        GradingService.AutoGradeResult result = gradingService.callPythonAutoGrading(submissionId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/python/knowledge-analysis/{studentId}")
    public ResponseEntity<GradingService.KnowledgeAnalysisResult> callPythonKnowledgeAnalysis(@PathVariable Long studentId) {
        GradingService.KnowledgeAnalysisResult result = gradingService.callPythonKnowledgeAnalysis(studentId);
        return ResponseEntity.ok(result);
    }
}
