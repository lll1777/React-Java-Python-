package com.education.assignment.controller;

import com.education.assignment.entity.LearningReport;
import com.education.assignment.service.LearningReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LearningReportController {
    private final LearningReportService learningReportService;

    @PostMapping("/generate/{studentId}")
    public ResponseEntity<LearningReport> generateReport(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "WEEKLY") LearningReport.ReportType reportType) {
        LearningReport report = learningReportService.generateReport(studentId, reportType);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<LearningReport>> getReportsByStudentId(@PathVariable Long studentId) {
        List<LearningReport> reports = learningReportService.getReportsByStudentId(studentId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LearningReport> getReportById(@PathVariable Long id) {
        LearningReport report = learningReportService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/student/{studentId}/latest")
    public ResponseEntity<LearningReport> getLatestReport(@PathVariable Long studentId) {
        LearningReport report = learningReportService.getLatestReport(studentId);
        return ResponseEntity.ok(report);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        learningReportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
