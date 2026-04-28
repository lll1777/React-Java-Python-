package com.education.assignment.controller;

import com.education.assignment.entity.WrongQuestion;
import com.education.assignment.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wrong-questions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WrongQuestionController {
    private final WrongQuestionService wrongQuestionService;

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<WrongQuestion>> getWrongQuestionsByStudentId(@PathVariable Long studentId) {
        List<WrongQuestion> questions = wrongQuestionService.getWrongQuestionsByStudentId(studentId);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/student/{studentId}/unresolved")
    public ResponseEntity<List<WrongQuestion>> getUnresolvedWrongQuestions(@PathVariable Long studentId) {
        List<WrongQuestion> questions = wrongQuestionService.getUnresolvedWrongQuestions(studentId);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/student/{studentId}/resolved")
    public ResponseEntity<List<WrongQuestion>> getResolvedWrongQuestions(@PathVariable Long studentId) {
        List<WrongQuestion> questions = wrongQuestionService.getResolvedWrongQuestions(studentId);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WrongQuestion> getWrongQuestionById(@PathVariable Long id) {
        WrongQuestion question = wrongQuestionService.getWrongQuestionById(id);
        return ResponseEntity.ok(question);
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<WrongQuestion> markAsResolved(
            @PathVariable Long id,
            @RequestBody(required = false) String notes) {
        WrongQuestion question = wrongQuestionService.markAsResolved(id, notes);
        return ResponseEntity.ok(question);
    }

    @PutMapping("/{id}/notes")
    public ResponseEntity<WrongQuestion> updateNotes(
            @PathVariable Long id,
            @RequestBody(required = false) String notes) {
        WrongQuestion question = wrongQuestionService.updateNotes(id, notes);
        return ResponseEntity.ok(question);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWrongQuestion(@PathVariable Long id) {
        wrongQuestionService.deleteWrongQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/student/{studentId}/count/unresolved")
    public ResponseEntity<Long> countUnresolvedByStudentId(@PathVariable Long studentId) {
        long count = wrongQuestionService.countUnresolvedByStudentId(studentId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/student/{studentId}/count/resolved")
    public ResponseEntity<Long> countResolvedByStudentId(@PathVariable Long studentId) {
        long count = wrongQuestionService.countResolvedByStudentId(studentId);
        return ResponseEntity.ok(count);
    }
}
