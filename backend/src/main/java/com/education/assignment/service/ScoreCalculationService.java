package com.education.assignment.service;

import com.education.assignment.entity.*;
import com.education.assignment.enums.QuestionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ScoreCalculationService {

    public static class QuestionScoreResult {
        private QuestionType questionType;
        private Integer questionScore;
        private Integer effectiveScore;
        private Boolean isCorrect;
        private String studentAnswer;
        private String correctAnswer;
        private String feedback;

        public QuestionType getQuestionType() { return questionType; }
        public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }
        public Integer getQuestionScore() { return questionScore; }
        public void setQuestionScore(Integer questionScore) { this.questionScore = questionScore; }
        public Integer getEffectiveScore() { return effectiveScore; }
        public void setEffectiveScore(Integer effectiveScore) { this.effectiveScore = effectiveScore; }
        public Boolean getIsCorrect() { return isCorrect; }
        public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
        public String getStudentAnswer() { return studentAnswer; }
        public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
    }

    public static class TypeScoreSummary {
        private QuestionType questionType;
        private Integer totalQuestions = 0;
        private Integer answeredQuestions = 0;
        private Integer correctQuestions = 0;
        private Integer wrongQuestions = 0;
        private Integer gradedQuestions = 0;
        private Integer totalScore = 0;
        private Integer maxScore = 0;
        private Double accuracy;
        private Double scoreRate;

        public QuestionType getQuestionType() { return questionType; }
        public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }
        public Integer getTotalQuestions() { return totalQuestions; }
        public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
        public Integer getAnsweredQuestions() { return answeredQuestions; }
        public void setAnsweredQuestions(Integer answeredQuestions) { this.answeredQuestions = answeredQuestions; }
        public Integer getCorrectQuestions() { return correctQuestions; }
        public void setCorrectQuestions(Integer correctQuestions) { this.correctQuestions = correctQuestions; }
        public Integer getWrongQuestions() { return wrongQuestions; }
        public void setWrongQuestions(Integer wrongQuestions) { this.wrongQuestions = wrongQuestions; }
        public Integer getGradedQuestions() { return gradedQuestions; }
        public void setGradedQuestions(Integer gradedQuestions) { this.gradedQuestions = gradedQuestions; }
        public Integer getTotalScore() { return totalScore; }
        public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
        public Integer getMaxScore() { return maxScore; }
        public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }
        public Double getAccuracy() { return accuracy; }
        public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
        public Double getScoreRate() { return scoreRate; }
        public void setScoreRate(Double scoreRate) { this.scoreRate = scoreRate; }
    }

    public static class SubmissionScoreResult {
        private Integer autoScore = 0;
        private Integer manualScore = 0;
        private Integer totalScore = 0;
        
        private Integer objectiveScore = 0;
        private Integer objectiveMaxScore = 0;
        private Double objectiveAccuracy;
        private Integer totalObjectiveQuestions = 0;
        private Integer correctObjectiveQuestions = 0;
        private Integer wrongObjectiveQuestions = 0;
        private Integer unansweredObjectiveQuestions = 0;
        
        private Integer subjectiveScore = 0;
        private Integer subjectiveMaxScore = 0;
        private Double subjectiveScoreRate;
        private Integer totalSubjectiveQuestions = 0;
        private Integer gradedSubjectiveQuestions = 0;
        private Integer ungradedSubjectiveQuestions = 0;
        
        private Integer totalQuestions = 0;
        private Integer answeredQuestions = 0;
        private Integer unansweredQuestions = 0;
        private Integer assignmentTotalScore = 0;
        private Double totalScoreRate;
        
        private Map<QuestionType, TypeScoreSummary> typeSummaries = new EnumMap<>(QuestionType.class);
        private List<ValidationError> validationErrors = new ArrayList<>();
        private boolean isValid = true;

        public Integer getAutoScore() { return autoScore; }
        public void setAutoScore(Integer autoScore) { this.autoScore = autoScore; }
        public Integer getManualScore() { return manualScore; }
        public void setManualScore(Integer manualScore) { this.manualScore = manualScore; }
        public Integer getTotalScore() { return totalScore; }
        public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
        public Integer getObjectiveScore() { return objectiveScore; }
        public void setObjectiveScore(Integer objectiveScore) { this.objectiveScore = objectiveScore; }
        public Integer getObjectiveMaxScore() { return objectiveMaxScore; }
        public void setObjectiveMaxScore(Integer objectiveMaxScore) { this.objectiveMaxScore = objectiveMaxScore; }
        public Double getObjectiveAccuracy() { return objectiveAccuracy; }
        public void setObjectiveAccuracy(Double objectiveAccuracy) { this.objectiveAccuracy = objectiveAccuracy; }
        public Integer getTotalObjectiveQuestions() { return totalObjectiveQuestions; }
        public void setTotalObjectiveQuestions(Integer totalObjectiveQuestions) { this.totalObjectiveQuestions = totalObjectiveQuestions; }
        public Integer getCorrectObjectiveQuestions() { return correctObjectiveQuestions; }
        public void setCorrectObjectiveQuestions(Integer correctObjectiveQuestions) { this.correctObjectiveQuestions = correctObjectiveQuestions; }
        public Integer getWrongObjectiveQuestions() { return wrongObjectiveQuestions; }
        public void setWrongObjectiveQuestions(Integer wrongObjectiveQuestions) { this.wrongObjectiveQuestions = wrongObjectiveQuestions; }
        public Integer getUnansweredObjectiveQuestions() { return unansweredObjectiveQuestions; }
        public void setUnansweredObjectiveQuestions(Integer unansweredObjectiveQuestions) { this.unansweredObjectiveQuestions = unansweredObjectiveQuestions; }
        public Integer getSubjectiveScore() { return subjectiveScore; }
        public void setSubjectiveScore(Integer subjectiveScore) { this.subjectiveScore = subjectiveScore; }
        public Integer getSubjectiveMaxScore() { return subjectiveMaxScore; }
        public void setSubjectiveMaxScore(Integer subjectiveMaxScore) { this.subjectiveMaxScore = subjectiveMaxScore; }
        public Double getSubjectiveScoreRate() { return subjectiveScoreRate; }
        public void setSubjectiveScoreRate(Double subjectiveScoreRate) { this.subjectiveScoreRate = subjectiveScoreRate; }
        public Integer getTotalSubjectiveQuestions() { return totalSubjectiveQuestions; }
        public void setTotalSubjectiveQuestions(Integer totalSubjectiveQuestions) { this.totalSubjectiveQuestions = totalSubjectiveQuestions; }
        public Integer getGradedSubjectiveQuestions() { return gradedSubjectiveQuestions; }
        public void setGradedSubjectiveQuestions(Integer gradedSubjectiveQuestions) { this.gradedSubjectiveQuestions = gradedSubjectiveQuestions; }
        public Integer getUngradedSubjectiveQuestions() { return ungradedSubjectiveQuestions; }
        public void setUngradedSubjectiveQuestions(Integer ungradedSubjectiveQuestions) { this.ungradedSubjectiveQuestions = ungradedSubjectiveQuestions; }
        public Integer getTotalQuestions() { return totalQuestions; }
        public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
        public Integer getAnsweredQuestions() { return answeredQuestions; }
        public void setAnsweredQuestions(Integer answeredQuestions) { this.answeredQuestions = answeredQuestions; }
        public Integer getUnansweredQuestions() { return unansweredQuestions; }
        public void setUnansweredQuestions(Integer unansweredQuestions) { this.unansweredQuestions = unansweredQuestions; }
        public Integer getAssignmentTotalScore() { return assignmentTotalScore; }
        public void setAssignmentTotalScore(Integer assignmentTotalScore) { this.assignmentTotalScore = assignmentTotalScore; }
        public Double getTotalScoreRate() { return totalScoreRate; }
        public void setTotalScoreRate(Double totalScoreRate) { this.totalScoreRate = totalScoreRate; }
        public Map<QuestionType, TypeScoreSummary> getTypeSummaries() { return typeSummaries; }
        public void setTypeSummaries(Map<QuestionType, TypeScoreSummary> typeSummaries) { this.typeSummaries = typeSummaries; }
        public List<ValidationError> getValidationErrors() { return validationErrors; }
        public void setValidationErrors(List<ValidationError> validationErrors) { this.validationErrors = validationErrors; }
        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }
    }

    public static class ValidationError {
        private String field;
        private String errorType;
        private String message;
        private Object expected;
        private Object actual;

        public ValidationError(String field, String errorType, String message, Object expected, Object actual) {
            this.field = field;
            this.errorType = errorType;
            this.message = message;
            this.expected = expected;
            this.actual = actual;
        }

        public String getField() { return field; }
        public String getErrorType() { return errorType; }
        public String getMessage() { return message; }
        public Object getExpected() { return expected; }
        public Object getActual() { return actual; }
    }

    public QuestionScoreResult calculateSingleQuestionScore(Question question, SubmissionAnswer answer) {
        QuestionScoreResult result = new QuestionScoreResult();
        result.setQuestionType(question.getType());
        result.setQuestionScore(question.getScore());
        result.setStudentAnswer(answer.getStudentAnswer());
        result.setCorrectAnswer(question.getCorrectAnswer());
        
        String studentAnswer = answer.getStudentAnswer();
        String correctAnswer = question.getCorrectAnswer();
        
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            result.setEffectiveScore(0);
            result.setIsCorrect(false);
            result.setFeedback("未作答");
            return result;
        }
        
        if (!question.getAutoGradable() || correctAnswer == null || correctAnswer.trim().isEmpty()) {
            result.setEffectiveScore(answer.getManualScore() != null ? answer.getManualScore() : 0);
            result.setIsCorrect(null);
            result.setFeedback("主观题，需要手动批改");
            return result;
        }
        
        switch (question.getType()) {
            case SINGLE_CHOICE:
                return calculateSingleChoiceScore(question, answer, result);
            case MULTIPLE_CHOICE:
                return calculateMultipleChoiceScore(question, answer, result);
            case TRUE_FALSE:
                return calculateTrueFalseScore(question, answer, result);
            case FILL_BLANK:
                return calculateFillBlankScore(question, answer, result);
            default:
                result.setEffectiveScore(answer.getManualScore() != null ? answer.getManualScore() : 0);
                result.setIsCorrect(null);
                result.setFeedback("非自动批改题型");
                return result;
        }
    }

    private QuestionScoreResult calculateSingleChoiceScore(Question question, SubmissionAnswer answer, QuestionScoreResult result) {
        String sa = answer.getStudentAnswer().trim().toUpperCase();
        String ca = question.getCorrectAnswer().trim().toUpperCase();
        
        if (sa.equals(ca)) {
            result.setEffectiveScore(question.getScore());
            result.setIsCorrect(true);
            result.setFeedback("答案正确");
        } else {
            result.setEffectiveScore(0);
            result.setIsCorrect(false);
            result.setFeedback("答案错误，正确答案：" + ca);
        }
        return result;
    }

    private QuestionScoreResult calculateMultipleChoiceScore(Question question, SubmissionAnswer answer, QuestionScoreResult result) {
        Set<String> studentOptions = parseMultipleChoiceAnswer(answer.getStudentAnswer());
        Set<String> correctOptions = parseMultipleChoiceAnswer(question.getCorrectAnswer());
        
        if (studentOptions.isEmpty()) {
            result.setEffectiveScore(0);
            result.setIsCorrect(false);
            result.setFeedback("未选择任何选项");
            return result;
        }
        
        Set<String> correctSelected = new HashSet<>(studentOptions);
        correctSelected.retainAll(correctOptions);
        
        Set<String> wrongSelected = new HashSet<>(studentOptions);
        wrongSelected.removeAll(correctOptions);
        
        Set<String> missed = new HashSet<>(correctOptions);
        missed.removeAll(studentOptions);
        
        if (correctSelected.equals(correctOptions) && wrongSelected.isEmpty()) {
            result.setEffectiveScore(question.getScore());
            result.setIsCorrect(true);
            result.setFeedback("答案完全正确");
        } else if (!wrongSelected.isEmpty()) {
            result.setEffectiveScore(0);
            result.setIsCorrect(false);
            result.setFeedback("选择了错误选项：" + wrongSelected);
        } else {
            double correctRatio = (double) correctSelected.size() / correctOptions.size();
            int partialScore = (int) Math.round(question.getScore() * correctRatio * 0.5);
            result.setEffectiveScore(partialScore);
            result.setIsCorrect(false);
            result.setFeedback("部分正确，漏选：" + missed + "，得分：" + partialScore + "/" + question.getScore());
        }
        return result;
    }

    private QuestionScoreResult calculateTrueFalseScore(Question question, SubmissionAnswer answer, QuestionScoreResult result) {
        String sa = normalizeTrueFalse(answer.getStudentAnswer());
        String ca = normalizeTrueFalse(question.getCorrectAnswer());
        
        if (sa == null || ca == null) {
            result.setEffectiveScore(0);
            result.setIsCorrect(false);
            result.setFeedback("答案格式不正确");
            return result;
        }
        
        if (sa.equals(ca)) {
            result.setEffectiveScore(question.getScore());
            result.setIsCorrect(true);
            result.setFeedback("答案正确");
        } else {
            result.setEffectiveScore(0);
            result.setIsCorrect(false);
            result.setFeedback("答案错误，正确答案：" + ca);
        }
        return result;
    }

    private QuestionScoreResult calculateFillBlankScore(Question question, SubmissionAnswer answer, QuestionScoreResult result) {
        String sa = answer.getStudentAnswer().trim();
        String ca = question.getCorrectAnswer().trim();
        
        if (sa.equalsIgnoreCase(ca)) {
            result.setEffectiveScore(question.getScore());
            result.setIsCorrect(true);
            result.setFeedback("答案完全正确");
            return result;
        }
        
        double similarity = calculateSimilarity(sa, ca);
        if (similarity >= 0.9) {
            result.setEffectiveScore(question.getScore());
            result.setIsCorrect(true);
            result.setFeedback("答案正确（相似匹配）");
        } else if (similarity >= 0.8) {
            int score = (int) Math.round(question.getScore() * 0.8);
            result.setEffectiveScore(score);
            result.setIsCorrect(false);
            result.setFeedback("答案相似，得分：" + score + "/" + question.getScore());
        } else if (similarity >= 0.6) {
            int score = (int) Math.round(question.getScore() * 0.5);
            result.setEffectiveScore(score);
            result.setIsCorrect(false);
            result.setFeedback("答案部分相似，得分：" + score + "/" + question.getScore());
        } else {
            result.setEffectiveScore(0);
            result.setIsCorrect(false);
            result.setFeedback("答案错误，正确答案：" + ca);
        }
        return result;
    }

    public SubmissionScoreResult calculateSubmissionScores(List<Question> questions, List<SubmissionAnswer> answers) {
        SubmissionScoreResult result = new SubmissionScoreResult();
        
        Map<Long, SubmissionAnswer> answerMap = new HashMap<>();
        for (SubmissionAnswer answer : answers) {
            answerMap.put(answer.getQuestion().getId(), answer);
        }
        
        int assignmentTotalScore = 0;
        for (Question q : questions) {
            assignmentTotalScore += q.getScore();
        }
        result.setAssignmentTotalScore(assignmentTotalScore);
        result.setTotalQuestions(questions.size());
        
        int answeredCount = 0;
        
        for (Question question : questions) {
            SubmissionAnswer answer = answerMap.get(question.getId());
            QuestionType type = question.getType();
            
            TypeScoreSummary typeSummary = result.getTypeSummaries().computeIfAbsent(type, k -> {
                TypeScoreSummary ts = new TypeScoreSummary();
                ts.setQuestionType(type);
                return ts;
            });
            
            typeSummary.setTotalQuestions(typeSummary.getTotalQuestions() + 1);
            typeSummary.setMaxScore(typeSummary.getMaxScore() + question.getScore());
            
            if (question.getAutoGradable()) {
                result.setTotalObjectiveQuestions(result.getTotalObjectiveQuestions() + 1);
                result.setObjectiveMaxScore(result.getObjectiveMaxScore() + question.getScore());
            } else {
                result.setTotalSubjectiveQuestions(result.getTotalSubjectiveQuestions() + 1);
                result.setSubjectiveMaxScore(result.getSubjectiveMaxScore() + question.getScore());
            }
            
            if (answer != null && answer.getStudentAnswer() != null && !answer.getStudentAnswer().trim().isEmpty()) {
                answeredCount++;
                typeSummary.setAnsweredQuestions(typeSummary.getAnsweredQuestions() + 1);
                
                QuestionScoreResult qs = calculateSingleQuestionScore(question, answer);
                
                if (question.getAutoGradable()) {
                    Integer autoScore = answer.getAutoScore() != null ? answer.getAutoScore() : 
                                       (qs.getEffectiveScore() != null ? qs.getEffectiveScore() : 0);
                    result.setAutoScore(result.getAutoScore() + autoScore);
                    result.setObjectiveScore(result.getObjectiveScore() + autoScore);
                    typeSummary.setTotalScore(typeSummary.getTotalScore() + autoScore);
                    
                    Boolean isCorrect = answer.getIsCorrect() != null ? answer.getIsCorrect() : qs.getIsCorrect();
                    if (Boolean.TRUE.equals(isCorrect)) {
                        result.setCorrectObjectiveQuestions(result.getCorrectObjectiveQuestions() + 1);
                        typeSummary.setCorrectQuestions(typeSummary.getCorrectQuestions() + 1);
                    } else if (Boolean.FALSE.equals(isCorrect)) {
                        result.setWrongObjectiveQuestions(result.getWrongObjectiveQuestions() + 1);
                        typeSummary.setWrongQuestions(typeSummary.getWrongQuestions() + 1);
                    }
                } else {
                    if (answer.getManualScore() != null) {
                        result.setManualScore(result.getManualScore() + answer.getManualScore());
                        result.setSubjectiveScore(result.getSubjectiveScore() + answer.getManualScore());
                        typeSummary.setTotalScore(typeSummary.getTotalScore() + answer.getManualScore());
                        result.setGradedSubjectiveQuestions(result.getGradedSubjectiveQuestions() + 1);
                        typeSummary.setGradedQuestions(typeSummary.getGradedQuestions() + 1);
                    } else {
                        result.setUngradedSubjectiveQuestions(result.getUngradedSubjectiveQuestions() + 1);
                    }
                }
            } else {
                if (question.getAutoGradable()) {
                    result.setUnansweredObjectiveQuestions(result.getUnansweredObjectiveQuestions() + 1);
                }
            }
        }
        
        result.setAnsweredQuestions(answeredCount);
        result.setUnansweredQuestions(questions.size() - answeredCount);
        
        result.setTotalScore(result.getAutoScore() + result.getManualScore());
        
        if (result.getObjectiveMaxScore() > 0) {
            result.setObjectiveAccuracy((double) result.getObjectiveScore() / result.getObjectiveMaxScore() * 100);
        }
        
        if (result.getSubjectiveMaxScore() > 0 && result.getGradedSubjectiveQuestions().equals(result.getTotalSubjectiveQuestions())) {
            result.setSubjectiveScoreRate((double) result.getSubjectiveScore() / result.getSubjectiveMaxScore() * 100);
        }
        
        if (result.getAssignmentTotalScore() > 0) {
            result.setTotalScoreRate((double) result.getTotalScore() / result.getAssignmentTotalScore() * 100);
        }
        
        for (TypeScoreSummary summary : result.getTypeSummaries().values()) {
            if (summary.getMaxScore() > 0) {
                summary.setScoreRate((double) summary.getTotalScore() / summary.getMaxScore() * 100);
            }
            if (summary.getAnsweredQuestions() > 0 && isObjectiveType(summary.getQuestionType())) {
                summary.setAccuracy((double) summary.getCorrectQuestions() / summary.getAnsweredQuestions() * 100);
            }
        }
        
        validateScores(result, questions, answers);
        
        return result;
    }

    private boolean isObjectiveType(QuestionType type) {
        return type == QuestionType.SINGLE_CHOICE ||
               type == QuestionType.MULTIPLE_CHOICE ||
               type == QuestionType.TRUE_FALSE ||
               type == QuestionType.FILL_BLANK;
    }

    private void validateScores(SubmissionScoreResult result, List<Question> questions, List<SubmissionAnswer> answers) {
        List<ValidationError> errors = new ArrayList<>();
        
        int expectedTotal = result.getAutoScore() + result.getManualScore();
        if (!result.getTotalScore().equals(expectedTotal)) {
            errors.add(new ValidationError("totalScore", "MISMATCH", 
                "总分与自动分+手工分之和不一致", expectedTotal, result.getTotalScore()));
        }
        
        int expectedObjective = 0;
        int expectedSubjective = 0;
        for (TypeScoreSummary summary : result.getTypeSummaries().values()) {
            if (isObjectiveType(summary.getQuestionType())) {
                expectedObjective += summary.getTotalScore();
            } else {
                expectedSubjective += summary.getTotalScore();
            }
        }
        
        if (!result.getObjectiveScore().equals(expectedObjective)) {
            errors.add(new ValidationError("objectiveScore", "MISMATCH",
                "客观题总分与题型汇总不一致", expectedObjective, result.getObjectiveScore()));
        }
        
        if (!result.getSubjectiveScore().equals(expectedSubjective)) {
            errors.add(new ValidationError("subjectiveScore", "MISMATCH",
                "主观题总分与题型汇总不一致", expectedSubjective, result.getSubjectiveScore()));
        }
        
        int expectedTotalQuestions = result.getTotalObjectiveQuestions() + result.getTotalSubjectiveQuestions();
        if (!result.getTotalQuestions().equals(expectedTotalQuestions)) {
            errors.add(new ValidationError("totalQuestions", "MISMATCH",
                "总题数与客观+主观题数不一致", expectedTotalQuestions, result.getTotalQuestions()));
        }
        
        if (!errors.isEmpty()) {
            result.setValid(false);
            result.setValidationErrors(errors);
            log.warn("分数计算校验失败：{}", errors);
        }
    }

    public SubmissionScoreResult recalculateAndFixScores(List<Question> questions, List<SubmissionAnswer> answers) {
        SubmissionScoreResult result = calculateSubmissionScores(questions, answers);
        
        if (!result.isValid()) {
            log.warn("发现分数计算错误，执行自动修复...");
            
            int autoScore = 0;
            int manualScore = 0;
            int objectiveScore = 0;
            int subjectiveScore = 0;
            
            for (SubmissionAnswer answer : answers) {
                if (answer.getQuestion() == null) continue;
                Question q = answer.getQuestion();
                
                if (q.getAutoGradable() && answer.getAutoScore() != null) {
                    autoScore += answer.getAutoScore();
                    objectiveScore += answer.getAutoScore();
                } else if (answer.getManualScore() != null) {
                    manualScore += answer.getManualScore();
                    subjectiveScore += answer.getManualScore();
                }
            }
            
            int totalScore = autoScore + manualScore;
            
            result.setAutoScore(autoScore);
            result.setManualScore(manualScore);
            result.setTotalScore(totalScore);
            result.setObjectiveScore(objectiveScore);
            result.setSubjectiveScore(subjectiveScore);
            
            if (result.getObjectiveMaxScore() > 0) {
                result.setObjectiveAccuracy((double) objectiveScore / result.getObjectiveMaxScore() * 100);
            }
            if (result.getSubjectiveMaxScore() > 0 && result.getGradedSubjectiveQuestions().equals(result.getTotalSubjectiveQuestions())) {
                result.setSubjectiveScoreRate((double) subjectiveScore / result.getSubjectiveMaxScore() * 100);
            }
            if (result.getAssignmentTotalScore() > 0) {
                result.setTotalScoreRate((double) totalScore / result.getAssignmentTotalScore() * 100);
            }
            
            result.setValid(true);
            result.getValidationErrors().add(new ValidationError("AUTO_FIX", "FIX", "执行了自动分数修复", null, null));
            log.info("分数自动修复完成");
        }
        
        return result;
    }

    public void applyScoresToSubmission(Submission submission, SubmissionScoreResult result) {
        submission.setAutoScore(result.getAutoScore());
        submission.setManualScore(result.getManualScore());
        submission.setTotalScore(result.getTotalScore());
        
        submission.setObjectiveScore(result.getObjectiveScore());
        submission.setObjectiveMaxScore(result.getObjectiveMaxScore());
        submission.setSubjectiveScore(result.getSubjectiveScore());
        submission.setSubjectiveMaxScore(result.getSubjectiveMaxScore());
        submission.setAssignmentTotalScore(result.getAssignmentTotalScore());
        
        submission.setObjectiveAccuracy(result.getObjectiveAccuracy());
        submission.setSubjectiveScoreRate(result.getSubjectiveScoreRate());
        submission.setTotalScoreRate(result.getTotalScoreRate());
        
        submission.setTotalObjectiveQuestions(result.getTotalObjectiveQuestions());
        submission.setCorrectObjectiveQuestions(result.getCorrectObjectiveQuestions());
        submission.setWrongObjectiveQuestions(result.getWrongObjectiveQuestions());
        submission.setUnansweredObjectiveQuestions(result.getUnansweredObjectiveQuestions());
        
        submission.setTotalSubjectiveQuestions(result.getTotalSubjectiveQuestions());
        submission.setGradedSubjectiveQuestions(result.getGradedSubjectiveQuestions());
        submission.setUngradedSubjectiveQuestions(result.getUngradedSubjectiveQuestions());
        
        submission.setTotalQuestions(result.getTotalQuestions());
        submission.setAnsweredQuestions(result.getAnsweredQuestions());
        submission.setUnansweredQuestions(result.getUnansweredQuestions());
        
        for (Map.Entry<QuestionType, TypeScoreSummary> entry : result.getTypeSummaries().entrySet()) {
            TypeScoreSummary summary = entry.getValue();
            applyTypeSummary(submission, entry.getKey(), summary);
        }
    }

    private void applyTypeSummary(Submission submission, QuestionType type, TypeScoreSummary summary) {
        switch (type) {
            case SINGLE_CHOICE:
                submission.setSingleChoiceScore(summary.getTotalScore());
                submission.setSingleChoiceMaxScore(summary.getMaxScore());
                submission.setSingleChoiceCorrect(summary.getCorrectQuestions());
                submission.setSingleChoiceTotal(summary.getTotalQuestions());
                break;
            case MULTIPLE_CHOICE:
                submission.setMultipleChoiceScore(summary.getTotalScore());
                submission.setMultipleChoiceMaxScore(summary.getMaxScore());
                submission.setMultipleChoiceCorrect(summary.getCorrectQuestions());
                submission.setMultipleChoiceTotal(summary.getTotalQuestions());
                break;
            case TRUE_FALSE:
                submission.setTrueFalseScore(summary.getTotalScore());
                submission.setTrueFalseMaxScore(summary.getMaxScore());
                submission.setTrueFalseCorrect(summary.getCorrectQuestions());
                submission.setTrueFalseTotal(summary.getTotalQuestions());
                break;
            case FILL_BLANK:
                submission.setFillBlankScore(summary.getTotalScore());
                submission.setFillBlankMaxScore(summary.getMaxScore());
                submission.setFillBlankCorrect(summary.getCorrectQuestions());
                submission.setFillBlankTotal(summary.getTotalQuestions());
                break;
            case SHORT_ANSWER:
                submission.setShortAnswerScore(summary.getTotalScore());
                submission.setShortAnswerMaxScore(summary.getMaxScore());
                submission.setShortAnswerGraded(summary.getGradedQuestions());
                submission.setShortAnswerTotal(summary.getTotalQuestions());
                break;
            case ESSAY:
                submission.setEssayScore(summary.getTotalScore());
                submission.setEssayMaxScore(summary.getMaxScore());
                submission.setEssayGraded(summary.getGradedQuestions());
                submission.setEssayTotal(summary.getTotalQuestions());
                break;
            case CODING:
                submission.setCodingScore(summary.getTotalScore());
                submission.setCodingMaxScore(summary.getMaxScore());
                submission.setCodingGraded(summary.getGradedQuestions());
                submission.setCodingTotal(summary.getTotalQuestions());
                break;
        }
    }

    public void applyScoresToVersion(SubmissionVersion version, SubmissionScoreResult result) {
        version.setAutoScore(result.getAutoScore());
        version.setManualScore(result.getManualScore());
        version.setTotalScore(result.getTotalScore());
        
        version.setObjectiveScore(result.getObjectiveScore());
        version.setObjectiveMaxScore(result.getObjectiveMaxScore());
        version.setSubjectiveScore(result.getSubjectiveScore());
        version.setSubjectiveMaxScore(result.getSubjectiveMaxScore());
        version.setAssignmentTotalScore(result.getAssignmentTotalScore());
        
        version.setObjectiveAccuracy(result.getObjectiveAccuracy());
        version.setSubjectiveScoreRate(result.getSubjectiveScoreRate());
        version.setTotalScoreRate(result.getTotalScoreRate());
        
        version.setTotalObjectiveQuestions(result.getTotalObjectiveQuestions());
        version.setCorrectObjectiveQuestions(result.getCorrectObjectiveQuestions());
        version.setWrongObjectiveQuestions(result.getWrongObjectiveQuestions());
        
        version.setTotalSubjectiveQuestions(result.getTotalSubjectiveQuestions());
        version.setGradedSubjectiveQuestions(result.getGradedSubjectiveQuestions());
        
        version.setTotalQuestions(result.getTotalQuestions());
        version.setAnsweredQuestions(result.getAnsweredQuestions());
        
        for (Map.Entry<QuestionType, TypeScoreSummary> entry : result.getTypeSummaries().entrySet()) {
            TypeScoreSummary summary = entry.getValue();
            applyTypeSummaryToVersion(version, entry.getKey(), summary);
        }
    }

    private void applyTypeSummaryToVersion(SubmissionVersion version, QuestionType type, TypeScoreSummary summary) {
        switch (type) {
            case SINGLE_CHOICE:
                version.setSingleChoiceScore(summary.getTotalScore());
                version.setSingleChoiceMaxScore(summary.getMaxScore());
                version.setSingleChoiceCorrect(summary.getCorrectQuestions());
                version.setSingleChoiceTotal(summary.getTotalQuestions());
                break;
            case MULTIPLE_CHOICE:
                version.setMultipleChoiceScore(summary.getTotalScore());
                version.setMultipleChoiceMaxScore(summary.getMaxScore());
                version.setMultipleChoiceCorrect(summary.getCorrectQuestions());
                version.setMultipleChoiceTotal(summary.getTotalQuestions());
                break;
            case TRUE_FALSE:
                version.setTrueFalseScore(summary.getTotalScore());
                version.setTrueFalseMaxScore(summary.getMaxScore());
                version.setTrueFalseCorrect(summary.getCorrectQuestions());
                version.setTrueFalseTotal(summary.getTotalQuestions());
                break;
            case FILL_BLANK:
                version.setFillBlankScore(summary.getTotalScore());
                version.setFillBlankMaxScore(summary.getMaxScore());
                version.setFillBlankCorrect(summary.getCorrectQuestions());
                version.setFillBlankTotal(summary.getTotalQuestions());
                break;
            case SHORT_ANSWER:
                version.setShortAnswerScore(summary.getTotalScore());
                version.setShortAnswerMaxScore(summary.getMaxScore());
                version.setShortAnswerGraded(summary.getGradedQuestions());
                version.setShortAnswerTotal(summary.getTotalQuestions());
                break;
            case ESSAY:
                version.setEssayScore(summary.getTotalScore());
                version.setEssayMaxScore(summary.getMaxScore());
                version.setEssayGraded(summary.getGradedQuestions());
                version.setEssayTotal(summary.getTotalQuestions());
                break;
            case CODING:
                version.setCodingScore(summary.getTotalScore());
                version.setCodingMaxScore(summary.getMaxScore());
                version.setCodingGraded(summary.getGradedQuestions());
                version.setCodingTotal(summary.getTotalQuestions());
                break;
        }
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

    private String normalizeTrueFalse(String answer) {
        if (answer == null) {
            return null;
        }
        
        String lower = answer.trim().toLowerCase();
        
        if (Arrays.asList("true", "t", "1", "yes", "是", "对", "正确").contains(lower)) {
            return "TRUE";
        }
        
        if (Arrays.asList("false", "f", "0", "no", "否", "错", "错误").contains(lower)) {
            return "FALSE";
        }
        
        return lower.toUpperCase();
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
}
