package com.education.assignment.controller;

import com.education.assignment.dto.SubmissionCreateDTO;
import com.education.assignment.entity.Submission;
import com.education.assignment.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SubmissionController {
    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<Submission> submitAssignment(
            @Valid @RequestBody SubmissionCreateDTO dto,
            @RequestParam Long studentId) {
        Submission submission = submissionService.submitAssignment(dto, studentId);
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Submission> getSubmissionById(@PathVariable Long id) {
        Submission submission = submissionService.getSubmissionById(id);
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<Submission>> getSubmissionsByAssignmentId(@PathVariable Long assignmentId) {
        List<Submission> submissions = submissionService.getSubmissionsByAssignmentId(assignmentId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Submission>> getSubmissionsByStudentId(@PathVariable Long studentId) {
        List<Submission> submissions = submissionService.getSubmissionsByStudentId(studentId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/student/{studentId}/graded")
    public ResponseEntity<List<Submission>> getGradedSubmissionsByStudentId(@PathVariable Long studentId) {
        List<Submission> submissions = submissionService.getGradedSubmissionsByStudentId(studentId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/assignment/{assignmentId}/student/{studentId}")
    public ResponseEntity<Submission> getSubmissionByAssignmentAndStudent(
            @PathVariable Long assignmentId,
            @PathVariable Long studentId) {
        Submission submission = submissionService.getSubmissionByAssignmentAndStudent(assignmentId, studentId);
        return ResponseEntity.ok(submission);
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<Submission> returnSubmission(@PathVariable Long id) {
        Submission submission = submissionService.returnSubmission(id);
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/assignment/{assignmentId}/statistics")
    public ResponseEntity<SubmissionStatistics> getAssignmentStatistics(@PathVariable Long assignmentId) {
        long count = submissionService.countSubmissionsByAssignmentId(assignmentId);
        Double avg = submissionService.getAverageScoreByAssignmentId(assignmentId);
        Integer highest = submissionService.getHighestScoreByAssignmentId(assignmentId);
        Integer lowest = submissionService.getLowestScoreByAssignmentId(assignmentId);
        
        SubmissionStatistics stats = new SubmissionStatistics();
        stats.setTotalSubmissions(count);
        stats.setAverageScore(avg != null ? avg : 0.0);
        stats.setHighestScore(highest != null ? highest : 0);
        stats.setLowestScore(lowest != null ? lowest : 0);
        
        return ResponseEntity.ok(stats);
    }

    public static class SubmissionStatistics {
        private long totalSubmissions;
        private Double averageScore;
        private Integer highestScore;
        private Integer lowestScore;

        public long getTotalSubmissions() { return totalSubmissions; }
        public void setTotalSubmissions(long totalSubmissions) { this.totalSubmissions = totalSubmissions; }
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
        public Integer getHighestScore() { return highestScore; }
        public void setHighestScore(Integer highestScore) { this.highestScore = highestScore; }
        public Integer getLowestScore() { return lowestScore; }
        public void setLowestScore(Integer lowestScore) { this.lowestScore = lowestScore; }
    }
}
