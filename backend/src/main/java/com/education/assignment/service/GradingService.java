package com.education.assignment.service;

import com.education.assignment.dto.GradingDTO;
import com.education.assignment.entity.*;
import com.education.assignment.enums.AssignmentStatus;
import com.education.assignment.enums.QuestionType;
import com.education.assignment.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GradingService {
    private final SubmissionRepository submissionRepository;
    private final SubmissionVersionRepository submissionVersionRepository;
    private final SubmissionAnswerRepository submissionAnswerRepository;
    private final QuestionRepository questionRepository;
    private final WrongQuestionRepository wrongQuestionRepository;
    private final SubmissionService submissionService;
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
    public GradingResult performAutoGrading(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
        
        if (submission.getStatus() != AssignmentStatus.AUTO_GRADING) {
            throw new RuntimeException("作业状态不正确");
        }
        
        List<SubmissionAnswer> answers = submissionAnswerRepository.findBySubmissionId(submissionId);
        List<Question> allQuestions = questionRepository.findByAssignmentIdOrderByOrderIndexAsc(
                submission.getAssignment().getId());
        
        int objectiveScore = 0;
        int objectiveMaxScore = 0;
        int correctObjectiveCount = 0;
        int totalObjectiveCount = 0;
        int totalQuestionsAnswered = 0;
        
        Map<Long, Question> questionMap = new HashMap<>();
        for (Question q : allQuestions) {
            questionMap.put(q.getId(), q);
        }
        
        for (SubmissionAnswer answer : answers) {
            Question question = questionMap.get(answer.getQuestion().getId());
            if (question == null) {
                question = answer.getQuestion();
            }
            
            totalQuestionsAnswered++;
            
            if (question.getAutoGradable()) {
                totalObjectiveCount++;
                objectiveMaxScore += question.getScore();
                
                if (question.getCorrectAnswer() != null && !question.getCorrectAnswer().isEmpty()) {
                    String studentAnswer = answer.getStudentAnswer();
                    Integer score = gradeAnswer(question, studentAnswer);
                    
                    answer.setAutoScore(score);
                    
                    boolean isCorrect = (score != null && score.equals(question.getScore()));
                    answer.setIsCorrect(isCorrect);
                    
                    if (score != null) {
                        objectiveScore += score;
                    }
                    
                    if (isCorrect) {
                        correctObjectiveCount++;
                    } else {
                        updateWrongQuestion(submission.getStudent(), question, answer);
                    }
                    
                    submissionAnswerRepository.save(answer);
                }
            }
        }
        
        int wrongObjectiveCount = totalObjectiveCount - correctObjectiveCount;
        
        submission.setAutoScore(objectiveScore);
        submission.setObjectiveScore(objectiveScore);
        submission.setObjectiveMaxScore(objectiveMaxScore);
        submission.setTotalObjectiveQuestions(totalObjectiveCount);
        submission.setCorrectObjectiveQuestions(correctObjectiveCount);
        submission.setWrongObjectiveQuestions(wrongObjectiveCount);
        
        if (objectiveMaxScore > 0) {
            double accuracy = (double) objectiveScore / objectiveMaxScore * 100;
            submission.setObjectiveAccuracy(accuracy);
        }
        
        submission.setAutoGradedAt(LocalDateTime.now());
        submission.setStatus(AssignmentStatus.AUTO_GRADED);
        
        Submission savedSubmission = submissionRepository.save(submission);
        
        updateLatestVersion(savedSubmission);
        
        GradingResult result = new GradingResult();
        result.setSuccess(true);
        result.setObjectiveScore(objectiveScore);
        result.setObjectiveMaxScore(objectiveMaxScore);
        result.setCorrectObjectiveCount(correctObjectiveCount);
        result.setTotalObjectiveCount(totalObjectiveCount);
        result.setWrongObjectiveCount(wrongObjectiveCount);
        result.setObjectiveAccuracy(objectiveMaxScore > 0 ? (double) objectiveScore / objectiveMaxScore * 100 : 0.0);
        result.setTotalQuestions(allQuestions.size());
        result.setAnsweredQuestions(totalQuestionsAnswered);
        
        return result;
    }

    private Integer gradeAnswer(Question question, String studentAnswer) {
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            return 0;
        }
        
        QuestionType type = question.getType();
        String correctAnswer = question.getCorrectAnswer();
        
        if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
            return null;
        }
        
        switch (type) {
            case SINGLE_CHOICE:
                return gradeSingleChoice(studentAnswer, correctAnswer, question.getScore());
            
            case MULTIPLE_CHOICE:
                return gradeMultipleChoice(studentAnswer, correctAnswer, question.getScore());
            
            case TRUE_FALSE:
                return gradeTrueFalse(studentAnswer, correctAnswer, question.getScore());
            
            case FILL_BLANK:
                return gradeFillBlank(studentAnswer, correctAnswer, question.getScore());
            
            case SHORT_ANSWER:
            case ESSAY:
            case CODING:
                return null;
            
            default:
                return null;
        }
    }

    private Integer gradeSingleChoice(String studentAnswer, String correctAnswer, Integer maxScore) {
        String sa = studentAnswer.trim().toUpperCase();
        String ca = correctAnswer.trim().toUpperCase();
        return sa.equals(ca) ? maxScore : 0;
    }

    private Integer gradeMultipleChoice(String studentAnswer, String correctAnswer, Integer maxScore) {
        Set<String> studentOptions = parseMultipleChoiceAnswer(studentAnswer);
        Set<String> correctOptions = parseMultipleChoiceAnswer(correctAnswer);
        
        if (studentOptions.isEmpty()) {
            return 0;
        }
        
        Set<String> correctSelected = new HashSet<>(studentOptions);
        correctSelected.retainAll(correctOptions);
        
        Set<String> wrongSelected = new HashSet<>(studentOptions);
        wrongSelected.removeAll(correctOptions);
        
        Set<String> missed = new HashSet<>(correctOptions);
        missed.removeAll(studentOptions);
        
        if (correctSelected.equals(correctOptions) && wrongSelected.isEmpty()) {
            return maxScore;
        }
        
        if (!wrongSelected.isEmpty()) {
            return 0;
        }
        
        double correctRatio = (double) correctSelected.size() / correctOptions.size();
        return (int) Math.round(maxScore * correctRatio * 0.5);
    }

    private Set<String> parseMultipleChoiceAnswer(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return new HashSet<>();
        }
        
        Set<String> options = new HashSet<>();
        String[] parts = answer.split("[,;，；\\s]+");
        for (String part : parts) {
            String trimmed = part.trim().toUpperCase();
            if (!trimmed.isEmpty()) {
                options.add(trimmed);
            }
        }
        return options;
    }

    private Integer gradeTrueFalse(String studentAnswer, String correctAnswer, Integer maxScore) {
        String sa = normalizeTrueFalse(studentAnswer);
        String ca = normalizeTrueFalse(correctAnswer);
        
        if (sa == null || ca == null) {
            return 0;
        }
        
        return sa.equals(ca) ? maxScore : 0;
    }

    private String normalizeTrueFalse(String answer) {
        if (answer == null) {
            return null;
        }
        
        String lower = answer.trim().toLowerCase();
        
        if (Arrays.asList("true", "t", "1", "yes", "对", "正确").contains(lower)) {
            return "TRUE";
        }
        
        if (Arrays.asList("false", "f", "0", "no", "错", "错误").contains(lower)) {
            return "FALSE";
        }
        
        return lower.toUpperCase();
    }

    private Integer gradeFillBlank(String studentAnswer, String correctAnswer, Integer maxScore) {
        String sa = studentAnswer.trim();
        String ca = correctAnswer.trim();
        
        if (sa.equalsIgnoreCase(ca)) {
            return maxScore;
        }
        
        double similarity = calculateSimilarity(sa, ca);
        if (similarity >= 0.9) {
            return maxScore;
        }
        if (similarity >= 0.8) {
            return (int) Math.round(maxScore * 0.8);
        }
        if (similarity >= 0.6) {
            return (int) Math.round(maxScore * 0.5);
        }
        
        return 0;
    }

    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        
        String str1 = s1.toLowerCase();
        String str2 = s2.toLowerCase();
        
        if (str1.equals(str2)) {
            return 1.0;
        }
        
        int maxLen = Math.max(str1.length(), str2.length());
        if (maxLen == 0) {
            return 1.0;
        }
        
        int distance = levenshteinDistance(str1, str2);
        return (double) (maxLen - distance) / maxLen;
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        
        return dp[s1.length()][s2.length()];
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
    public GradingResult performManualGrading(Long submissionId, GradingDTO gradingDTO) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
        
        if (submission.getStatus() != AssignmentStatus.MANUAL_GRADING && 
            submission.getStatus() != AssignmentStatus.AUTO_GRADED) {
            throw new RuntimeException("作业状态不正确");
        }
        
        List<Question> allQuestions = questionRepository.findByAssignmentIdOrderByOrderIndexAsc(
                submission.getAssignment().getId());
        
        Map<Long, Question> questionMap = new HashMap<>();
        int subjectiveMaxScore = 0;
        int totalSubjectiveCount = 0;
        
        for (Question q : allQuestions) {
            questionMap.put(q.getId(), q);
            if (!q.getAutoGradable()) {
                subjectiveMaxScore += q.getScore();
                totalSubjectiveCount++;
            }
        }
        
        int subjectiveScore = 0;
        int gradedSubjectiveCount = 0;
        
        if (gradingDTO.getQuestions() != null) {
            for (GradingDTO.QuestionGradingDTO qg : gradingDTO.getQuestions()) {
                SubmissionAnswer answer = submissionAnswerRepository.findById(qg.getSubmissionAnswerId())
                        .orElse(null);
                
                if (answer != null) {
                    answer.setManualScore(qg.getScore());
                    answer.setTeacherFeedback(qg.getFeedback());
                    
                    if (qg.getScore() != null) {
                        subjectiveScore += qg.getScore();
                        gradedSubjectiveCount++;
                    }
                    
                    submissionAnswerRepository.save(answer);
                }
            }
        }
        
        submission.setManualScore(subjectiveScore);
        submission.setSubjectiveScore(subjectiveScore);
        submission.setSubjectiveMaxScore(subjectiveMaxScore);
        submission.setTotalSubjectiveQuestions(totalSubjectiveCount);
        submission.setGradedSubjectiveQuestions(gradedSubjectiveCount);
        
        if (subjectiveMaxScore > 0 && gradedSubjectiveCount == totalSubjectiveCount) {
            double scoreRate = (double) subjectiveScore / subjectiveMaxScore * 100;
            submission.setSubjectiveScoreRate(scoreRate);
        }
        
        Integer autoScore = submission.getAutoScore() != null ? submission.getAutoScore() : 0;
        Integer objectiveScore = submission.getObjectiveScore() != null ? submission.getObjectiveScore() : 0;
        
        int totalScore = autoScore + subjectiveScore;
        submission.setTotalScore(totalScore);
        
        if (gradingDTO.getTeacherComments() != null) {
            submission.setTeacherComments(gradingDTO.getTeacherComments());
        }
        
        submission.setManuallyGradedAt(LocalDateTime.now());
        submission.setStatus(AssignmentStatus.GRADED);
        
        Submission savedSubmission = submissionRepository.save(submission);
        
        updateLatestVersion(savedSubmission);
        
        GradingResult result = new GradingResult();
        result.setSuccess(true);
        result.setObjectiveScore(objectiveScore);
        result.setObjectiveMaxScore(submission.getObjectiveMaxScore());
        result.setCorrectObjectiveCount(submission.getCorrectObjectiveQuestions());
        result.setTotalObjectiveCount(submission.getTotalObjectiveQuestions());
        result.setWrongObjectiveCount(submission.getWrongObjectiveQuestions());
        result.setObjectiveAccuracy(submission.getObjectiveAccuracy());
        result.setSubjectiveScore(subjectiveScore);
        result.setSubjectiveMaxScore(subjectiveMaxScore);
        result.setGradedSubjectiveCount(gradedSubjectiveCount);
        result.setTotalSubjectiveCount(totalSubjectiveCount);
        result.setSubjectiveScoreRate(submission.getSubjectiveScoreRate());
        result.setTotalScore(totalScore);
        
        return result;
    }

    @Transactional
    public GradingResult completeGrading(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
        
        if (submission.getStatus() != AssignmentStatus.AUTO_GRADED &&
            submission.getStatus() != AssignmentStatus.MANUAL_GRADING) {
            throw new RuntimeException("作业状态不正确");
        }
        
        submissionService.updateSubmissionScores(submission);
        
        Submission savedSubmission = submissionRepository.findById(submissionId).orElse(submission);
        
        savedSubmission.setStatus(AssignmentStatus.GRADED);
        savedSubmission = submissionRepository.save(savedSubmission);
        
        updateLatestVersion(savedSubmission);
        
        GradingResult result = new GradingResult();
        result.setSuccess(true);
        result.setObjectiveScore(savedSubmission.getObjectiveScore());
        result.setObjectiveMaxScore(savedSubmission.getObjectiveMaxScore());
        result.setCorrectObjectiveCount(savedSubmission.getCorrectObjectiveQuestions());
        result.setTotalObjectiveCount(savedSubmission.getTotalObjectiveQuestions());
        result.setWrongObjectiveCount(savedSubmission.getWrongObjectiveQuestions());
        result.setObjectiveAccuracy(savedSubmission.getObjectiveAccuracy());
        result.setSubjectiveScore(savedSubmission.getSubjectiveScore());
        result.setSubjectiveMaxScore(savedSubmission.getSubjectiveMaxScore());
        result.setGradedSubjectiveCount(savedSubmission.getGradedSubjectiveQuestions());
        result.setTotalSubjectiveCount(savedSubmission.getTotalSubjectiveQuestions());
        result.setSubjectiveScoreRate(savedSubmission.getSubjectiveScoreRate());
        result.setTotalScore(savedSubmission.getTotalScore());
        
        return result;
    }

    private void updateLatestVersion(Submission submission) {
        Optional<SubmissionVersion> latestVersionOpt = 
                submissionVersionRepository.findBySubmissionIdAndIsLatestTrue(submission.getId());
        
        if (latestVersionOpt.isPresent()) {
            SubmissionVersion version = latestVersionOpt.get();
            version.setAutoScore(submission.getAutoScore());
            version.setManualScore(submission.getManualScore());
            version.setTotalScore(submission.getTotalScore());
            version.setObjectiveScore(submission.getObjectiveScore());
            version.setObjectiveMaxScore(submission.getObjectiveMaxScore());
            version.setSubjectiveScore(submission.getSubjectiveScore());
            version.setSubjectiveMaxScore(submission.getSubjectiveMaxScore());
            version.setObjectiveAccuracy(submission.getObjectiveAccuracy());
            version.setSubjectiveScoreRate(submission.getSubjectiveScoreRate());
            version.setAutoGradedAt(submission.getAutoGradedAt());
            version.setManuallyGradedAt(submission.getManuallyGradedAt());
            version.setStatus(submission.getStatus());
            submissionVersionRepository.save(version);
        }
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

    public static class GradingResult {
        private boolean success;
        private int objectiveScore;
        private int objectiveMaxScore;
        private int correctObjectiveCount;
        private int totalObjectiveCount;
        private int wrongObjectiveCount;
        private Double objectiveAccuracy;
        private int subjectiveScore;
        private int subjectiveMaxScore;
        private int gradedSubjectiveCount;
        private int totalSubjectiveCount;
        private Double subjectiveScoreRate;
        private int totalScore;
        private int totalQuestions;
        private int answeredQuestions;
        private String message;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public int getObjectiveScore() { return objectiveScore; }
        public void setObjectiveScore(int objectiveScore) { this.objectiveScore = objectiveScore; }
        public int getObjectiveMaxScore() { return objectiveMaxScore; }
        public void setObjectiveMaxScore(int objectiveMaxScore) { this.objectiveMaxScore = objectiveMaxScore; }
        public int getCorrectObjectiveCount() { return correctObjectiveCount; }
        public void setCorrectObjectiveCount(int correctObjectiveCount) { this.correctObjectiveCount = correctObjectiveCount; }
        public int getTotalObjectiveCount() { return totalObjectiveCount; }
        public void setTotalObjectiveCount(int totalObjectiveCount) { this.totalObjectiveCount = totalObjectiveCount; }
        public int getWrongObjectiveCount() { return wrongObjectiveCount; }
        public void setWrongObjectiveCount(int wrongObjectiveCount) { this.wrongObjectiveCount = wrongObjectiveCount; }
        public Double getObjectiveAccuracy() { return objectiveAccuracy; }
        public void setObjectiveAccuracy(Double objectiveAccuracy) { this.objectiveAccuracy = objectiveAccuracy; }
        public int getSubjectiveScore() { return subjectiveScore; }
        public void setSubjectiveScore(int subjectiveScore) { this.subjectiveScore = subjectiveScore; }
        public int getSubjectiveMaxScore() { return subjectiveMaxScore; }
        public void setSubjectiveMaxScore(int subjectiveMaxScore) { this.subjectiveMaxScore = subjectiveMaxScore; }
        public int getGradedSubjectiveCount() { return gradedSubjectiveCount; }
        public void setGradedSubjectiveCount(int gradedSubjectiveCount) { this.gradedSubjectiveCount = gradedSubjectiveCount; }
        public int getTotalSubjectiveCount() { return totalSubjectiveCount; }
        public void setTotalSubjectiveCount(int totalSubjectiveCount) { this.totalSubjectiveCount = totalSubjectiveCount; }
        public Double getSubjectiveScoreRate() { return subjectiveScoreRate; }
        public void setSubjectiveScoreRate(Double subjectiveScoreRate) { this.subjectiveScoreRate = subjectiveScoreRate; }
        public int getTotalScore() { return totalScore; }
        public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
        public int getTotalQuestions() { return totalQuestions; }
        public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
        public int getAnsweredQuestions() { return answeredQuestions; }
        public void setAnsweredQuestions(int answeredQuestions) { this.answeredQuestions = answeredQuestions; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
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
