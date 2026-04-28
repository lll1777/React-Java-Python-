package com.education.assignment.service;

import com.education.assignment.entity.WrongQuestion;
import com.education.assignment.repository.WrongQuestionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WrongQuestionService {
    private final WrongQuestionRepository wrongQuestionRepository;

    public List<WrongQuestion> getWrongQuestionsByStudentId(Long studentId) {
        return wrongQuestionRepository.findByStudentId(studentId);
    }

    public List<WrongQuestion> getUnresolvedWrongQuestions(Long studentId) {
        return wrongQuestionRepository.findByStudentIdAndIsResolvedOrderByLastWrongAtDesc(studentId, false);
    }

    public List<WrongQuestion> getResolvedWrongQuestions(Long studentId) {
        return wrongQuestionRepository.findByStudentIdAndIsResolvedOrderByLastWrongAtDesc(studentId, true);
    }

    public WrongQuestion getWrongQuestionById(Long id) {
        return wrongQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("错题不存在"));
    }

    @Transactional
    public WrongQuestion markAsResolved(Long id, String notes) {
        WrongQuestion wq = wrongQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("错题不存在"));
        
        wq.setIsResolved(true);
        wq.setResolvedAt(LocalDateTime.now());
        if (notes != null) {
            wq.setNotes(notes);
        }
        
        return wrongQuestionRepository.save(wq);
    }

    @Transactional
    public WrongQuestion updateNotes(Long id, String notes) {
        WrongQuestion wq = wrongQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("错题不存在"));
        
        wq.setNotes(notes);
        return wrongQuestionRepository.save(wq);
    }

    @Transactional
    public void deleteWrongQuestion(Long id) {
        WrongQuestion wq = wrongQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("错题不存在"));
        
        wrongQuestionRepository.delete(wq);
    }

    public long countUnresolvedByStudentId(Long studentId) {
        return wrongQuestionRepository.countByStudentIdAndIsResolved(studentId, false);
    }

    public long countResolvedByStudentId(Long studentId) {
        return wrongQuestionRepository.countByStudentIdAndIsResolved(studentId, true);
    }
}
