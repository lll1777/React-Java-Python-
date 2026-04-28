package com.education.assignment.controller;

import com.education.assignment.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<StatisticsService.AssignmentStatistics> getAssignmentStatistics(
            @PathVariable Long assignmentId) {
        StatisticsService.AssignmentStatistics stats = statisticsService.getAssignmentStatistics(assignmentId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<StatisticsService.StudentStatistics> getStudentStatistics(
            @PathVariable Long studentId) {
        StatisticsService.StudentStatistics stats = statisticsService.getStudentStatistics(studentId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<StatisticsService.ClassStatistics> getClassStatistics(
            @PathVariable Long classId) {
        StatisticsService.ClassStatistics stats = statisticsService.getClassStatistics(classId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/knowledge-points/{studentId}")
    public ResponseEntity<List<StatisticsService.KnowledgePointStatistics>> getKnowledgePointStatistics(
            @PathVariable Long studentId) {
        List<StatisticsService.KnowledgePointStatistics> stats = statisticsService.getKnowledgePointStatistics(studentId);
        return ResponseEntity.ok(stats);
    }
}
