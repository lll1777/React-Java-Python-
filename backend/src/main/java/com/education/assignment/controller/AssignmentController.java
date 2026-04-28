package com.education.assignment.controller;

import com.education.assignment.dto.AssignmentCreateDTO;
import com.education.assignment.entity.Assignment;
import com.education.assignment.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssignmentController {
    private final AssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<Assignment> createAssignment(
            @Valid @RequestBody AssignmentCreateDTO dto,
            @RequestParam Long creatorId) {
        Assignment assignment = assignmentService.createAssignment(dto, creatorId);
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable Long id) {
        Assignment assignment = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<Assignment>> getAssignmentsByClassId(@PathVariable Long classId) {
        List<Assignment> assignments = assignmentService.getAssignmentsByClassId(classId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Assignment>> getAssignmentsByStudentId(@PathVariable Long studentId) {
        List<Assignment> assignments = assignmentService.getAssignmentsByStudentId(studentId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<Assignment>> getAssignmentsByCreatorId(@PathVariable Long creatorId) {
        List<Assignment> assignments = assignmentService.getAssignmentsByCreatorId(creatorId);
        return ResponseEntity.ok(assignments);
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Assignment> publishAssignment(@PathVariable Long id) {
        Assignment assignment = assignmentService.publishAssignment(id);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/{id}/open")
    public ResponseEntity<Assignment> openAssignment(@PathVariable Long id) {
        Assignment assignment = assignmentService.openAssignment(id);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<Assignment> archiveAssignment(@PathVariable Long id) {
        Assignment assignment = assignmentService.archiveAssignment(id);
        return ResponseEntity.ok(assignment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Assignment> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentCreateDTO dto) {
        Assignment assignment = assignmentService.updateAssignment(id, dto);
        return ResponseEntity.ok(assignment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
}
