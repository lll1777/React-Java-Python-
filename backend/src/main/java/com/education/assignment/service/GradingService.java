package com.education.assignment.service;

import com.education.assignment.dto.GradingDTO;
import com.education.assignment.entity.Question;
import com.education.assignment.entity.Submission;
import com.education.assignment.entity.SubmissionAnswer;
import com.education.assignment.enums.AssignmentStatus;
import com.education.assignment.enums.QuestionType;
import com.education.assignment.repository.QuestionRepository;
import com.education.assignment.repository.SubmissionAnswerRepository;
import com.education.assignment.repository.SubmissionRepository;
import com.education.assignment.repository.WrongQuestionRepository;
import com.education.assignment.entity.WrongQuestion;
import com.education.assignment.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GradingService {
    private final SubmissionRepository submissionRepository;
    private final SubmissionAnswerRepository submissionAnswerRepository;
    private final QuestionRepository questionRepository;
    private final WrongQuestionRepository wrongQuestionRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String PYTHON_AUTO_GRADING_URL = "http://localhost:5000/api/auto-grade";
    private static final String PYTHON_KNOWLEDGE_ANALYSIS_URL = "http://localhost:5000/api/knowledge-analysis";

    @Transactional
    public Submission startAutoGrading(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
        
        if (submission.getStatus() != AssignmentStatus.SUBMITTED) {
            throw new RuntimeException("只有已提交状态的作业可以开始自动批改");
        }
        
        submission.setStatus(AssignmentStatus.AUTO_GRADING);
        return submissionRepository.save(submission);
    }

    @Transactional
    public Submission performAutoGrading(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
        
        if (submission.getStatus() != AssignmentStatus.AUTO_GRADING) {
            throw new RuntimeException("作业状态不正确");
        }
        
        List<SubmissionAnswer> answers = submissionAnswerRepository.findBySubmissionId(submissionId);
        int totalAutoScore = 0;
        
        for (SubmissionAnswer answer : answers) {
            Question question = answer.getQuestion();
            
            if (question.getAutoGradable() && question.getCorrectAnswer() != null) {
                Integer score = gradeAnswer(question, answer.getStudentAnswer());
                answer.setAutoScore(score);
                answer.setIsCorrect(score != null && score.equals(question.getScore()));
                
                if (score != null) {
                    totalAutoScore += score;
                }
                
                if (Boolean.FALSE.equals(answer.getIsCorrect())) {
                    updateWrongQuestion(submission.getStudent(), question, answer);
                }
                
                submissionAnswerRepository.save(answer);
            }
        }
        
        submission.setAutoScore(totalAutoScore);
        submission.setAutoGradedAt(LocalDateTime.now());
        submission.setStatus(AssignmentStatus.AUTO_GRADED);
        
        return submissionRepository.save(submission);
    }

    private Integer gradeAnswer(Question question, String studentAnswer) {
        if (studentAnswer == null || studentAnswer.isEmpty()) {
            return 0;
        }
        
        QuestionType type = question.getType();
        String correctAnswer = question.getCorrectAnswer();
        
        switch (type) {
            case SINGLE_CHOICE:
            case TRUE_FALSE:
                return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim()) ? question.getScore() : 0;
            
            case MULTIPLE_CHOICE:
                String[] studentAnswers = studentAnswer.split("[,;]");
                String[] correctAnswers = correctAnswer.split("[,;]");
                
                if (studentAnswers.length != correctAnswers.length) {
                    return 0;
                }
                
                boolean allCorrect = true;
                for (String sa : studentAnswers) {
                    boolean found = false;
                    for (String ca : correctAnswers) {
                        if (sa.trim().equalsIgnoreCase(ca.trim())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        allCorrect = false;
                        break;
                    }
                }
                
                return allCorrect ? question.getScore() : 0;
            
            case FILL_BLANK:
                return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim()) ? question.getScore() : 0;
            
            case SHORT_ANSWER:
            case ESSAY:
            case CODING:
                return null;
            
            default:
                return null;
        }
    }

    private void updateWrongQuestion(User student, Question question, SubmissionAnswer answer) {
        Optional<WrongQuestion> existing = wrongQuestionRepository.findByStudentIdAndQuestionId(
                student.getId(), question.getId());
        
        if (existing.isPresent()) {
            WrongQuestion wq = existing.get();
            wq.setWrongCount(wq.getWrongCount() + 1);
            wq.setLastWrongAt(LocalDateTime.now());
            wq.setIsResolved(false);
            wq.setStudentAnswer(answer.getStudentAnswer());
            wrongQuestionRepository.save(wq);
        } else {
            WrongQuestion wq = new WrongQuestion();
            wq.setStudent(student);
            wq.setQuestion(question);
            wq.setSubmissionAnswer(answer);
            wq.setStudentAnswer(answer.getStudentAnswer());
            wq.setWrongCount(1);
            wq.setIsResolved(false);
            wq.setFirstWrongAt(LocalDateTime.now());
            wq.setLastWrongAt(LocalDateTime.now());
            wrongQuestionRepository.save(wq);
        }
    }

    @Transactional
    public Submission startManualGrading(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
        
        if (submission.getStatus() != AssignmentStatus.AUTO_GRADED) {
            throw new RuntimeException("只有自动批改完成状态的作业可以开始手工批改");
        }
        
        submission.setStatus(AssignmentStatus.MANUAL_GRADING);
        return submissionRepository.save(submission);
    }

    @Transactional
    public Submission performManualGrading(Long submissionId, GradingDTO gradingDTO) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
        
        if (submission.getStatus() != AssignmentStatus.MANUAL_GRADING && 
            submission.getStatus() != AssignmentStatus.AUTO_GRADED) {
            throw new RuntimeException("作业状态不正确");
        }
        
        int totalManualScore = 0;
        
        if (gradingDTO.getQuestions() != null) {
            for (GradingDTO.QuestionGradingDTO qg : gradingDTO.getQuestions()) {
                SubmissionAnswer answer = submissionAnswerRepository.findById(qg.getSubmissionAnswerId())
                        .orElse(null);
                
                if (answer != null) {
                    answer.setManualScore(qg.getScore());
                    answer.setTeacherFeedback(qg.getFeedback());
                    
                    if (qg.getScore() != null) {
                        totalManualScore += qg.getScore();
                    }
                    
                    submissionAnswerRepository.save(answer);
                }
            }
        }
        
        submission.setManualScore(totalManualScore);
        
        Integer autoScore = submission.getAutoScore() != null ? submission.getAutoScore() : 0;
        submission.setTotalScore(autoScore + totalManualScore);
        
        if (gradingDTO.getTeacherComments() != null) {
            submission.setTeacherComments(gradingDTO.getTeacherComments());
        }
        
        submission.setManuallyGradedAt(LocalDateTime.now());
        submission.setStatus(AssignmentStatus.GRADED);
        
        return submissionRepository.save(submission);
    }

    @Transactional
    public Submission completeGrading(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
        
        if (submission.getStatus() != AssignmentStatus.AUTO_GRADED &&
            submission.getStatus() != AssignmentStatus.MANUAL_GRADING) {
            throw new RuntimeException("作业状态不正确");
        }
        
        Integer autoScore = submission.getAutoScore() != null ? submission.getAutoScore() : 0;
        Integer manualScore = submission.getManualScore() != null ? submission.getManualScore() : 0;
        
        submission.setTotalScore(autoScore + manualScore);
        submission.setStatus(AssignmentStatus.GRADED);
        
        return submissionRepository.save(submission);
    }

    public AutoGradeResult callPythonAutoGrading(Long submissionId) {
        try {
            String url = PYTHON_AUTO_GRADING_URL + "/" + submissionId;
            return restTemplate.postForObject(url, null, AutoGradeResult.class);
        } catch (Exception e) {
            return null;
        }
    }

    public KnowledgeAnalysisResult callPythonKnowledgeAnalysis(Long studentId) {
        try {
            String url = PYTHON_KNOWLEDGE_ANALYSIS_URL + "/" + studentId;
            return restTemplate.postForObject(url, null, KnowledgeAnalysisResult.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static class AutoGradeResult {
        private boolean success;
        private int totalScore;
        private String message;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public int getTotalScore() { return totalScore; }
        public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class KnowledgeAnalysisResult {
        private boolean success;
        private String analysis;
        private String recommendations;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getAnalysis() { return analysis; }
        public void setAnalysis(String analysis) { this.analysis = analysis; }
        public String getRecommendations() { return recommendations; }
        public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
    }
}
