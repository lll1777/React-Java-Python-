package com.education.assignment.service;

import com.education.assignment.dto.SubmissionCreateDTO;
import com.education.assignment.entity.Assignment;
import com.education.assignment.entity.Question;
import com.education.assignment.entity.Submission;
import com.education.assignment.entity.SubmissionAnswer;
import com.education.assignment.entity.User;
import com.education.assignment.enums.AssignmentStatus;
import com.education.assignment.repository.AssignmentRepository;
import com.education.assignment.repository.QuestionRepository;
import com.education.assignment.repository.SubmissionAnswerRepository;
import com.education.assignment.repository.SubmissionRepository;
import com.education.assignment.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final SubmissionAnswerRepository submissionAnswerRepository;
    private final AssignmentRepository assignmentRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Submission submitAssignment(SubmissionCreateDTO dto, Long studentId) {
        Assignment assignment = assignmentRepository.findById(dto.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("作业不存在"));
        
        if (assignment.getStatus() != AssignmentStatus.OPEN) {
            throw new RuntimeException("作业未开放提交");
        }
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Optional<Submission> existingSubmission = submissionRepository.findByAssignmentAndStudent(assignment, student);
        if (existingSubmission.isPresent()) {
            throw new RuntimeException("您已提交过该作业");
        }
        
        boolean isLate = LocalDateTime.now().isAfter(assignment.getDeadline());
        
        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setStatus(AssignmentStatus.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setIsLate(isLate);
        
        Submission savedSubmission = submissionRepository.save(submission);
        
        if (dto.getAnswers() != null && !dto.getAnswers().isEmpty()) {
            Map<Long, Question> questionMap = new HashMap<>();
            for (Question q : questionRepository.findByAssignmentIdOrderByOrderIndexAsc(assignment.getId())) {
                questionMap.put(q.getId(), q);
            }
            
            for (SubmissionCreateDTO.AnswerDTO answerDTO : dto.getAnswers()) {
                Question question = questionMap.get(answerDTO.getQuestionId());
                if (question == null) continue;
                
                SubmissionAnswer answer = new SubmissionAnswer();
                answer.setSubmission(savedSubmission);
                answer.setQuestion(question);
                answer.setStudentAnswer(answerDTO.getStudentAnswer());
                submissionAnswerRepository.save(answer);
            }
        }
        
        return savedSubmission;
    }

    public Submission getSubmissionById(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
    }

    public Submission getSubmissionByAssignmentAndStudent(Long assignmentId, Long studentId) {
        return submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
    }

    public List<Submission> getSubmissionsByAssignmentId(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    public List<Submission> getSubmissionsByStudentId(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }

    public List<Submission> getGradedSubmissionsByStudentId(Long studentId) {
        return submissionRepository.findByStudentIdAndStatus(studentId, AssignmentStatus.GRADED);
    }

    @Transactional
    public Submission returnSubmission(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
        
        if (submission.getStatus() != AssignmentStatus.GRADED) {
            throw new RuntimeException("只有已批改的作业可以返回");
        }
        
        submission.setStatus(AssignmentStatus.RETURNED);
        submission.setReturnedAt(LocalDateTime.now());
        
        return submissionRepository.save(submission);
    }

    public long countSubmissionsByAssignmentId(Long assignmentId) {
        return submissionRepository.countByAssignmentId(assignmentId);
    }

    public Double getAverageScoreByAssignmentId(Long assignmentId) {
        return submissionRepository.getAverageScoreByAssignmentId(assignmentId);
    }

    public Integer getHighestScoreByAssignmentId(Long assignmentId) {
        return submissionRepository.getHighestScoreByAssignmentId(assignmentId);
    }

    public Integer getLowestScoreByAssignmentId(Long assignmentId) {
        return submissionRepository.getLowestScoreByAssignmentId(assignmentId);
    }
}
