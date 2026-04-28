package com.education.assignment.controller;

import com.education.assignment.dto.GradingDTO;
import com.education.assignment.entity.Submission;
import com.education.assignment.service.GradingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grading")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GradingController {
    private final GradingService gradingService;

    @PostMapping("/auto/start/{submissionId}")
    public ResponseEntity<Submission> startAutoGrading(@PathVariable Long submissionId) {
        Submission submission = gradingService.startAutoGrading(submissionId);
        return ResponseEntity.ok(submission);
    }

    @PostMapping("/auto/perform/{submissionId}")
    public ResponseEntity<Submission> performAutoGrading(@PathVariable Long submissionId) {
        Submission submission = gradingService.performAutoGrading(submissionId);
        return ResponseEntity.ok(submission);
    }

    @PostMapping("/manual/start/{submissionId}")
    public ResponseEntity<Submission> startManualGrading(@PathVariable Long submissionId) {
        Submission submission = gradingService.startManualGrading(submissionId);
        return ResponseEntity.ok(submission);
    }

    @PostMapping("/manual/perform/{submissionId}")
    public ResponseEntity<Submission> performManualGrading(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradingDTO gradingDTO) {
        Submission submission = gradingService.performManualGrading(submissionId, gradingDTO);
        return ResponseEntity.ok(submission);
    }

    @PostMapping("/complete/{submissionId}")
    public ResponseEntity<Submission> completeGrading(@PathVariable Long submissionId) {
        Submission submission = gradingService.completeGrading(submissionId);
        return ResponseEntity.ok(submission);
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
