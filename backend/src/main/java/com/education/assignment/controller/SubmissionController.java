package com.education.assignment.controller;

import com.education.assignment.dto.SubmissionCreateDTO;
import com.education.assignment.entity.Submission;
import com.education.assignment.entity.SubmissionVersion;
import com.education.assignment.service.ScoreCalculationService;
import com.education.assignment.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SubmissionController {
    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitAssignment(
            @Valid @RequestBody SubmissionCreateDTO dto,
            @RequestParam Long studentId,
            @RequestParam(required = false) String versionNote) {
        Submission submission = submissionService.submitAssignment(dto, studentId, versionNote);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("submission", submission);
        result.put("versionNumber", submission.getVersionNumber());
        result.put("message", "作业提交成功");
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSubmissionById(@PathVariable Long id) {
        Submission submission = submissionService.getSubmissionById(id);
        List<SubmissionVersion> versions = submissionService.getSubmissionVersions(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("submission", submission);
        result.put("versions", versions);
        result.put("versionCount", versions.size());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<List<SubmissionVersion>> getSubmissionVersions(@PathVariable Long id) {
        List<SubmissionVersion> versions = submissionService.getSubmissionVersions(id);
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/{id}/versions/{versionNumber}")
    public ResponseEntity<SubmissionVersion> getSubmissionVersion(
            @PathVariable Long id,
            @PathVariable Integer versionNumber) {
        SubmissionVersion version = submissionService.getSubmissionVersion(id, versionNumber);
        return ResponseEntity.ok(version);
    }

    @GetMapping("/{id}/versions/latest")
    public ResponseEntity<SubmissionVersion> getLatestVersion(@PathVariable Long id) {
        SubmissionVersion version = submissionService.getLatestVersion(id);
        return ResponseEntity.ok(version);
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
    public ResponseEntity<Map<String, Object>> getSubmissionByAssignmentAndStudent(
            @PathVariable Long assignmentId,
            @PathVariable Long studentId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Submission submission = submissionService.getSubmissionByAssignmentAndStudent(assignmentId, studentId);
            List<SubmissionVersion> versions = submissionService.getSubmissionVersions(submission.getId());
            
            result.put("success", true);
            result.put("exists", true);
            result.put("submission", submission);
            result.put("versions", versions);
            result.put("versionCount", versions.size());
        } catch (Exception e) {
            result.put("success", true);
            result.put("exists", false);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<Map<String, Object>> returnSubmission(@PathVariable Long id) {
        Submission submission = submissionService.returnSubmission(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("submission", submission);
        result.put("message", "作业已返回给学生");
        
        return ResponseEntity.ok(result);
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
