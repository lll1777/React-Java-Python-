package com.education.assignment.service;

import com.education.assignment.dto.GradingDTO;
import com.education.assignment.entity.*;
import com.education.assignment.enums.AssignmentStatus;
import com.education.assignment.enums.QuestionType;
import com.education.assignment.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradingService {
    private final SubmissionRepository submissionRepository;
    private final SubmissionVersionRepository submissionVersionRepository;
    private final SubmissionAnswerRepository submissionAnswerRepository;
    private final QuestionRepository questionRepository;
    private final WrongQuestionRepository wrongQuestionRepository;
    private final SubmissionService submissionService;
    private final ScoreCalculationService scoreCalculationService;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String PYTHON_AUTO_GRADING_URL = "http://localhost:5000/api/auto-grade";
    private static final String PYTHON_KNOWLEDGE_ANALYSIS_URL = "http://localhost:5000/api/knowledge-analysis";

    @Transactional
    public Submission startAutoGrading(Long submissionId) {
        log.info("开始自动批改，submissionId={}", submissionId);
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
        
        if (submission.getStatus() != AssignmentStatus.SUBMITTED) {
            throw new RuntimeException("只有已提交状态的作业可以开始自动批改");
        }
        
        submission.setStatus(AssignmentStatus.AUTO_GRADING);
        return submissionRepository.save(submission);
    }

    @Transactional
    public GradingResult performAutoGrading(Long submissionId) {
        log.info("执行自动批改，submissionId={}", submissionId);
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
        
        if (submission.getStatus() != AssignmentStatus.AUTO_GRADING) {
            throw new RuntimeException("作业状态不正确，当前状态：" + submission.getStatus());
        }
        
        List<SubmissionAnswer> answers = submissionAnswerRepository.findBySubmissionId(submissionId);
        List<Question> allQuestions = questionRepository.findByAssignmentIdOrderByOrderIndexAsc(
                submission.getAssignment().getId());
        
        Map<Long, Question> questionMap = new HashMap<>();
        for (Question q : allQuestions) {
            questionMap.put(q.getId(), q);
        }
        
        int processedCount = 0;
        int correctCount = 0;
        int wrongCount = 0;
        
        for (SubmissionAnswer answer : answers) {
            Question question = questionMap.get(answer.getQuestion().getId());
            if (question == null) {
                question = answer.getQuestion();
            }
            
            if (question.getAutoGradable()) {
                processedCount++;
                
                ScoreCalculationService.QuestionScoreResult qs = 
                        scoreCalculationService.calculateSingleQuestionScore(question, answer);
                
                answer.setAutoScore(qs.getEffectiveScore());
                answer.setIsCorrect(qs.getIsCorrect());
                
                if (Boolean.TRUE.equals(qs.getIsCorrect())) {
                    correctCount++;
                } else if (Boolean.FALSE.equals(qs.getIsCorrect())) {
                    wrongCount++;
                    updateWrongQuestion(submission.getStudent(), question, answer);
                }
                
                submissionAnswerRepository.save(answer);
                log.debug("自动批改题目 questionId={}, type={}, score={}/{}, isCorrect={}",
                        question.getId(), question.getType(), 
                        qs.getEffectiveScore(), question.getScore(), qs.getIsCorrect());
            }
        }
        
        submission.setAutoGradedAt(LocalDateTime.now());
        submission.setStatus(AssignmentStatus.AUTO_GRADED);
        
        submissionService.updateSubmissionScores(submission);
        
        Submission savedSubmission = submissionRepository.save(submission);
        submissionService.createVersionFromSubmission(savedSubmission, true);
        
        GradingResult result = buildGradingResult(savedSubmission);
        result.setSuccess(true);
        result.setMessage("自动批改完成，处理了 " + processedCount + " 道客观题");
        
        log.info("自动批改完成，submissionId={}, totalScore={}", submissionId, savedSubmission.getTotalScore());
        
        return result;
    }

    @Transactional
    public Submission startManualGrading(Long submissionId) {
        log.info("开始手工批改，submissionId={}", submissionId);
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
        
        if (submission.getStatus() != AssignmentStatus.AUTO_GRADED) {
            throw new RuntimeException("只有自动批改完成状态的作业可以开始手工批改");
        }
        
        submission.setStatus(AssignmentStatus.MANUAL_GRADING);
        return submissionRepository.save(submission);
    }

    @Transactional
    public GradingResult performManualGrading(Long submissionId, GradingDTO gradingDTO) {
        log.info("执行手工批改，submissionId={}", submissionId);
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
        
        if (submission.getStatus() != AssignmentStatus.MANUAL_GRADING && 
            submission.getStatus() != AssignmentStatus.AUTO_GRADED) {
            throw new RuntimeException("作业状态不正确，当前状态：" + submission.getStatus());
        }
        
        int gradedCount = 0;
        int totalSubjectiveScore = 0;
        
        if (gradingDTO.getQuestions() != null) {
            for (GradingDTO.QuestionGradingDTO qg : gradingDTO.getQuestions()) {
                SubmissionAnswer answer = submissionAnswerRepository.findById(qg.getSubmissionAnswerId())
                        .orElse(null);
                
                if (answer != null) {
                    answer.setManualScore(qg.getScore());
                    answer.setTeacherFeedback(qg.getFeedback());
                    
                    if (qg.getScore() != null) {
                        totalSubjectiveScore += qg.getScore();
                        gradedCount++;
                    }
                    
                    submissionAnswerRepository.save(answer);
                    log.debug("手工批改题目 submissionAnswerId={}, score={}", 
                            qg.getSubmissionAnswerId(), qg.getScore());
                }
            }
        }
        
        if (gradingDTO.getTeacherComments() != null) {
            submission.setTeacherComments(gradingDTO.getTeacherComments());
        }
        
        submission.setManuallyGradedAt(LocalDateTime.now());
        submission.setStatus(AssignmentStatus.GRADED);
        
        submissionService.updateSubmissionScores(submission);
        
        Submission savedSubmission = submissionRepository.save(submission);
        submissionService.createVersionFromSubmission(savedSubmission, true);
        
        GradingResult result = buildGradingResult(savedSubmission);
        result.setSuccess(true);
        result.setMessage("手工批改完成，批改了 " + gradedCount + " 道主观题");
        
        log.info("手工批改完成，submissionId={}, totalScore={}", submissionId, savedSubmission.getTotalScore());
        
        return result;
    }

    @Transactional
    public GradingResult completeGrading(Long submissionId) {
        log.info("完成批改流程，submissionId={}", submissionId);
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
        
        if (submission.getStatus() != AssignmentStatus.AUTO_GRADED &&
            submission.getStatus() != AssignmentStatus.MANUAL_GRADING) {
            throw new RuntimeException("作业状态不正确，当前状态：" + submission.getStatus());
        }
        
        submissionService.updateSubmissionScores(submission);
        submission.setStatus(AssignmentStatus.GRADED);
        
        Submission savedSubmission = submissionRepository.save(submission);
        submissionService.createVersionFromSubmission(savedSubmission, true);
        
        GradingResult result = buildGradingResult(savedSubmission);
        result.setSuccess(true);
        result.setMessage("批改流程已完成");
        
        log.info("批改流程完成，submissionId={}, totalScore={}", submissionId, savedSubmission.getTotalScore());
        
        return result;
    }

    private GradingResult buildGradingResult(Submission submission) {
        GradingResult result = new GradingResult();
        
        result.setObjectiveScore(safeInt(submission.getObjectiveScore()));
        result.setObjectiveMaxScore(safeInt(submission.getObjectiveMaxScore()));
        result.setCorrectObjectiveCount(safeInt(submission.getCorrectObjectiveQuestions()));
        result.setTotalObjectiveCount(safeInt(submission.getTotalObjectiveQuestions()));
        result.setWrongObjectiveCount(safeInt(submission.getWrongObjectiveQuestions()));
        result.setObjectiveAccuracy(submission.getObjectiveAccuracy());
        
        result.setSubjectiveScore(safeInt(submission.getSubjectiveScore()));
        result.setSubjectiveMaxScore(safeInt(submission.getSubjectiveMaxScore()));
        result.setGradedSubjectiveCount(safeInt(submission.getGradedSubjectiveQuestions()));
        result.setTotalSubjectiveCount(safeInt(submission.getTotalSubjectiveQuestions()));
        result.setSubjectiveScoreRate(submission.getSubjectiveScoreRate());
        
        result.setTotalScore(safeInt(submission.getTotalScore()));
        result.setAssignmentTotalScore(safeInt(submission.getAssignmentTotalScore()));
        result.setTotalScoreRate(submission.getTotalScoreRate());
        
        result.setTotalQuestions(safeInt(submission.getTotalQuestions()));
        result.setAnsweredQuestions(safeInt(submission.getAnsweredQuestions()));
        result.setUnansweredQuestions(safeInt(submission.getUnansweredQuestions()));
        
        result.setSingleChoiceScore(safeInt(submission.getSingleChoiceScore()));
        result.setSingleChoiceMaxScore(safeInt(submission.getSingleChoiceMaxScore()));
        result.setSingleChoiceCorrect(safeInt(submission.getSingleChoiceCorrect()));
        result.setSingleChoiceTotal(safeInt(submission.getSingleChoiceTotal()));
        
        result.setMultipleChoiceScore(safeInt(submission.getMultipleChoiceScore()));
        result.setMultipleChoiceMaxScore(safeInt(submission.getMultipleChoiceMaxScore()));
        result.setMultipleChoiceCorrect(safeInt(submission.getMultipleChoiceCorrect()));
        result.setMultipleChoiceTotal(safeInt(submission.getMultipleChoiceTotal()));
        
        result.setTrueFalseScore(safeInt(submission.getTrueFalseScore()));
        result.setTrueFalseMaxScore(safeInt(submission.getTrueFalseMaxScore()));
        result.setTrueFalseCorrect(safeInt(submission.getTrueFalseCorrect()));
        result.setTrueFalseTotal(safeInt(submission.getTrueFalseTotal()));
        
        result.setFillBlankScore(safeInt(submission.getFillBlankScore()));
        result.setFillBlankMaxScore(safeInt(submission.getFillBlankMaxScore()));
        result.setFillBlankCorrect(safeInt(submission.getFillBlankCorrect()));
        result.setFillBlankTotal(safeInt(submission.getFillBlankTotal()));
        
        result.setShortAnswerScore(safeInt(submission.getShortAnswerScore()));
        result.setShortAnswerMaxScore(safeInt(submission.getShortAnswerMaxScore()));
        result.setShortAnswerGraded(safeInt(submission.getShortAnswerGraded()));
        result.setShortAnswerTotal(safeInt(submission.getShortAnswerTotal()));
        
        result.setEssayScore(safeInt(submission.getEssayScore()));
        result.setEssayMaxScore(safeInt(submission.getEssayMaxScore()));
        result.setEssayGraded(safeInt(submission.getEssayGraded()));
        result.setEssayTotal(safeInt(submission.getEssayTotal()));
        
        result.setCodingScore(safeInt(submission.getCodingScore()));
        result.setCodingMaxScore(safeInt(submission.getCodingMaxScore()));
        result.setCodingGraded(safeInt(submission.getCodingGraded()));
        result.setCodingTotal(safeInt(submission.getCodingTotal()));
        
        result.setStatus(submission.getStatus());
        result.setSubmittedAt(submission.getSubmittedAt());
        result.setAutoGradedAt(submission.getAutoGradedAt());
        result.setManuallyGradedAt(submission.getManuallyGradedAt());
        result.setVersionNumber(submission.getVersionNumber());
        
        return result;
    }

    private int safeInt(Integer value) {
        return value != null ? value : 0;
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

    public AutoGradeResult callPythonAutoGrading(Long submissionId) {
        try {
            String url = PYTHON_AUTO_GRADING_URL + "/" + submissionId;
            return restTemplate.postForObject(url, null, AutoGradeResult.class);
        } catch (Exception e) {
            log.warn("调用 Python AI 自动批改服务失败: {}", e.getMessage());
            AutoGradeResult result = new AutoGradeResult();
            result.setSuccess(false);
            result.setMessage("AI 服务不可用，请使用内置自动批改");
            return result;
        }
    }

    public KnowledgeAnalysisResult callPythonKnowledgeAnalysis(Long studentId) {
        try {
            String url = PYTHON_KNOWLEDGE_ANALYSIS_URL + "/" + studentId;
            return restTemplate.postForObject(url, null, KnowledgeAnalysisResult.class);
        } catch (Exception e) {
            log.warn("调用 Python 知识点分析服务失败: {}", e.getMessage());
            KnowledgeAnalysisResult result = new KnowledgeAnalysisResult();
            result.setSuccess(false);
            result.setAnalysis("AI 服务不可用");
            result.setRecommendations("请确保 AI 服务已启动");
            return result;
        }
    }

    public static class GradingResult {
        private boolean success;
        private String message;
        
        private Integer objectiveScore;
        private Integer objectiveMaxScore;
        private Integer correctObjectiveCount;
        private Integer totalObjectiveCount;
        private Integer wrongObjectiveCount;
        private Double objectiveAccuracy;
        
        private Integer subjectiveScore;
        private Integer subjectiveMaxScore;
        private Integer gradedSubjectiveCount;
        private Integer totalSubjectiveCount;
        private Double subjectiveScoreRate;
        
        private Integer totalScore;
        private Integer assignmentTotalScore;
        private Double totalScoreRate;
        
        private Integer totalQuestions;
        private Integer answeredQuestions;
        private Integer unansweredQuestions;
        
        private Integer singleChoiceScore;
        private Integer singleChoiceMaxScore;
        private Integer singleChoiceCorrect;
        private Integer singleChoiceTotal;
        
        private Integer multipleChoiceScore;
        private Integer multipleChoiceMaxScore;
        private Integer multipleChoiceCorrect;
        private Integer multipleChoiceTotal;
        
        private Integer trueFalseScore;
        private Integer trueFalseMaxScore;
        private Integer trueFalseCorrect;
        private Integer trueFalseTotal;
        
        private Integer fillBlankScore;
        private Integer fillBlankMaxScore;
        private Integer fillBlankCorrect;
        private Integer fillBlankTotal;
        
        private Integer shortAnswerScore;
        private Integer shortAnswerMaxScore;
        private Integer shortAnswerGraded;
        private Integer shortAnswerTotal;
        
        private Integer essayScore;
        private Integer essayMaxScore;
        private Integer essayGraded;
        private Integer essayTotal;
        
        private Integer codingScore;
        private Integer codingMaxScore;
        private Integer codingGraded;
        private Integer codingTotal;
        
        private AssignmentStatus status;
        private LocalDateTime submittedAt;
        private LocalDateTime autoGradedAt;
        private LocalDateTime manuallyGradedAt;
        private Integer versionNumber;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Integer getObjectiveScore() { return objectiveScore; }
        public void setObjectiveScore(Integer objectiveScore) { this.objectiveScore = objectiveScore; }
        public Integer getObjectiveMaxScore() { return objectiveMaxScore; }
        public void setObjectiveMaxScore(Integer objectiveMaxScore) { this.objectiveMaxScore = objectiveMaxScore; }
        public Integer getCorrectObjectiveCount() { return correctObjectiveCount; }
        public void setCorrectObjectiveCount(Integer correctObjectiveCount) { this.correctObjectiveCount = correctObjectiveCount; }
        public Integer getTotalObjectiveCount() { return totalObjectiveCount; }
        public void setTotalObjectiveCount(Integer totalObjectiveCount) { this.totalObjectiveCount = totalObjectiveCount; }
        public Integer getWrongObjectiveCount() { return wrongObjectiveCount; }
        public void setWrongObjectiveCount(Integer wrongObjectiveCount) { this.wrongObjectiveCount = wrongObjectiveCount; }
        public Double getObjectiveAccuracy() { return objectiveAccuracy; }
        public void setObjectiveAccuracy(Double objectiveAccuracy) { this.objectiveAccuracy = objectiveAccuracy; }
        public Integer getSubjectiveScore() { return subjectiveScore; }
        public void setSubjectiveScore(Integer subjectiveScore) { this.subjectiveScore = subjectiveScore; }
        public Integer getSubjectiveMaxScore() { return subjectiveMaxScore; }
        public void setSubjectiveMaxScore(Integer subjectiveMaxScore) { this.subjectiveMaxScore = subjectiveMaxScore; }
        public Integer getGradedSubjectiveCount() { return gradedSubjectiveCount; }
        public void setGradedSubjectiveCount(Integer gradedSubjectiveCount) { this.gradedSubjectiveCount = gradedSubjectiveCount; }
        public Integer getTotalSubjectiveCount() { return totalSubjectiveCount; }
        public void setTotalSubjectiveCount(Integer totalSubjectiveCount) { this.totalSubjectiveCount = totalSubjectiveCount; }
        public Double getSubjectiveScoreRate() { return subjectiveScoreRate; }
        public void setSubjectiveScoreRate(Double subjectiveScoreRate) { this.subjectiveScoreRate = subjectiveScoreRate; }
        public Integer getTotalScore() { return totalScore; }
        public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
        public Integer getAssignmentTotalScore() { return assignmentTotalScore; }
        public void setAssignmentTotalScore(Integer assignmentTotalScore) { this.assignmentTotalScore = assignmentTotalScore; }
        public Double getTotalScoreRate() { return totalScoreRate; }
        public void setTotalScoreRate(Double totalScoreRate) { this.totalScoreRate = totalScoreRate; }
        public Integer getTotalQuestions() { return totalQuestions; }
        public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
        public Integer getAnsweredQuestions() { return answeredQuestions; }
        public void setAnsweredQuestions(Integer answeredQuestions) { this.answeredQuestions = answeredQuestions; }
        public Integer getUnansweredQuestions() { return unansweredQuestions; }
        public void setUnansweredQuestions(Integer unansweredQuestions) { this.unansweredQuestions = unansweredQuestions; }
        public Integer getSingleChoiceScore() { return singleChoiceScore; }
        public void setSingleChoiceScore(Integer singleChoiceScore) { this.singleChoiceScore = singleChoiceScore; }
        public Integer getSingleChoiceMaxScore() { return singleChoiceMaxScore; }
        public void setSingleChoiceMaxScore(Integer singleChoiceMaxScore) { this.singleChoiceMaxScore = singleChoiceMaxScore; }
        public Integer getSingleChoiceCorrect() { return singleChoiceCorrect; }
        public void setSingleChoiceCorrect(Integer singleChoiceCorrect) { this.singleChoiceCorrect = singleChoiceCorrect; }
        public Integer getSingleChoiceTotal() { return singleChoiceTotal; }
        public void setSingleChoiceTotal(Integer singleChoiceTotal) { this.singleChoiceTotal = singleChoiceTotal; }
        public Integer getMultipleChoiceScore() { return multipleChoiceScore; }
        public void setMultipleChoiceScore(Integer multipleChoiceScore) { this.multipleChoiceScore = multipleChoiceScore; }
        public Integer getMultipleChoiceMaxScore() { return multipleChoiceMaxScore; }
        public void setMultipleChoiceMaxScore(Integer multipleChoiceMaxScore) { this.multipleChoiceMaxScore = multipleChoiceMaxScore; }
        public Integer getMultipleChoiceCorrect() { return multipleChoiceCorrect; }
        public void setMultipleChoiceCorrect(Integer multipleChoiceCorrect) { this.multipleChoiceCorrect = multipleChoiceCorrect; }
        public Integer getMultipleChoiceTotal() { return multipleChoiceTotal; }
        public void setMultipleChoiceTotal(Integer multipleChoiceTotal) { this.multipleChoiceTotal = multipleChoiceTotal; }
        public Integer getTrueFalseScore() { return trueFalseScore; }
        public void setTrueFalseScore(Integer trueFalseScore) { this.trueFalseScore = trueFalseScore; }
        public Integer getTrueFalseMaxScore() { return trueFalseMaxScore; }
        public void setTrueFalseMaxScore(Integer trueFalseMaxScore) { this.trueFalseMaxScore = trueFalseMaxScore; }
        public Integer getTrueFalseCorrect() { return trueFalseCorrect; }
        public void setTrueFalseCorrect(Integer trueFalseCorrect) { this.trueFalseCorrect = trueFalseCorrect; }
        public Integer getTrueFalseTotal() { return trueFalseTotal; }
        public void setTrueFalseTotal(Integer trueFalseTotal) { this.trueFalseTotal = trueFalseTotal; }
        public Integer getFillBlankScore() { return fillBlankScore; }
        public void setFillBlankScore(Integer fillBlankScore) { this.fillBlankScore = fillBlankScore; }
        public Integer getFillBlankMaxScore() { return fillBlankMaxScore; }
        public void setFillBlankMaxScore(Integer fillBlankMaxScore) { this.fillBlankMaxScore = fillBlankMaxScore; }
        public Integer getFillBlankCorrect() { return fillBlankCorrect; }
        public void setFillBlankCorrect(Integer fillBlankCorrect) { this.fillBlankCorrect = fillBlankCorrect; }
        public Integer getFillBlankTotal() { return fillBlankTotal; }
        public void setFillBlankTotal(Integer fillBlankTotal) { this.fillBlankTotal = fillBlankTotal; }
        public Integer getShortAnswerScore() { return shortAnswerScore; }
        public void setShortAnswerScore(Integer shortAnswerScore) { this.shortAnswerScore = shortAnswerScore; }
        public Integer getShortAnswerMaxScore() { return shortAnswerMaxScore; }
        public void setShortAnswerMaxScore(Integer shortAnswerMaxScore) { this.shortAnswerMaxScore = shortAnswerMaxScore; }
        public Integer getShortAnswerGraded() { return shortAnswerGraded; }
        public void setShortAnswerGraded(Integer shortAnswerGraded) { this.shortAnswerGraded = shortAnswerGraded; }
        public Integer getShortAnswerTotal() { return shortAnswerTotal; }
        public void setShortAnswerTotal(Integer shortAnswerTotal) { this.shortAnswerTotal = shortAnswerTotal; }
        public Integer getEssayScore() { return essayScore; }
        public void setEssayScore(Integer essayScore) { this.essayScore = essayScore; }
        public Integer getEssayMaxScore() { return essayMaxScore; }
        public void setEssayMaxScore(Integer essayMaxScore) { this.essayMaxScore = essayMaxScore; }
        public Integer getEssayGraded() { return essayGraded; }
        public void setEssayGraded(Integer essayGraded) { this.essayGraded = essayGraded; }
        public Integer getEssayTotal() { return essayTotal; }
        public void setEssayTotal(Integer essayTotal) { this.essayTotal = essayTotal; }
        public Integer getCodingScore() { return codingScore; }
        public void setCodingScore(Integer codingScore) { this.codingScore = codingScore; }
        public Integer getCodingMaxScore() { return codingMaxScore; }
        public void setCodingMaxScore(Integer codingMaxScore) { this.codingMaxScore = codingMaxScore; }
        public Integer getCodingGraded() { return codingGraded; }
        public void setCodingGraded(Integer codingGraded) { this.codingGraded = codingGraded; }
        public Integer getCodingTotal() { return codingTotal; }
        public void setCodingTotal(Integer codingTotal) { this.codingTotal = codingTotal; }
        public AssignmentStatus getStatus() { return status; }
        public void setStatus(AssignmentStatus status) { this.status = status; }
        public LocalDateTime getSubmittedAt() { return submittedAt; }
        public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
        public LocalDateTime getAutoGradedAt() { return autoGradedAt; }
        public void setAutoGradedAt(LocalDateTime autoGradedAt) { this.autoGradedAt = autoGradedAt; }
        public LocalDateTime getManuallyGradedAt() { return manuallyGradedAt; }
        public void setManuallyGradedAt(LocalDateTime manuallyGradedAt) { this.manuallyGradedAt = manuallyGradedAt; }
        public Integer getVersionNumber() { return versionNumber; }
        public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
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
