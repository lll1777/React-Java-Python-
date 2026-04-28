package com.education.assignment.service;

import com.education.assignment.entity.Assignment;
import com.education.assignment.entity.Submission;
import com.education.assignment.entity.User;
import com.education.assignment.enums.AssignmentStatus;
import com.education.assignment.repository.AssignmentRepository;
import com.education.assignment.repository.ClazzRepository;
import com.education.assignment.repository.SubmissionAnswerRepository;
import com.education.assignment.repository.SubmissionRepository;
import com.education.assignment.repository.WrongQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final ClazzRepository clazzRepository;
    private final SubmissionAnswerRepository submissionAnswerRepository;
    private final WrongQuestionRepository wrongQuestionRepository;

    public AssignmentStatistics getAssignmentStatistics(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("作业不存在"));
        
        Long totalStudents = clazzRepository.countStudentsByClassId(assignment.getClazz().getId());
        Long submittedCount = submissionRepository.countByAssignmentId(assignmentId);
        Double averageScore = submissionRepository.getAverageScoreByAssignmentId(assignmentId);
        Integer highestScore = submissionRepository.getHighestScoreByAssignmentId(assignmentId);
        Integer lowestScore = submissionRepository.getLowestScoreByAssignmentId(assignmentId);
        
        AssignmentStatistics stats = new AssignmentStatistics();
        stats.setAssignmentId(assignmentId);
        stats.setAssignmentTitle(assignment.getTitle());
        stats.setTotalStudents(totalStudents.intValue());
        stats.setSubmittedCount(submittedCount.intValue());
        stats.setUnsubmittedCount(totalStudents.intValue() - submittedCount.intValue());
        stats.setSubmissionRate(totalStudents > 0 ? (double) submittedCount / totalStudents * 100 : 0.0);
        stats.setAverageScore(averageScore != null ? averageScore : 0.0);
        stats.setHighestScore(highestScore != null ? highestScore : 0);
        stats.setLowestScore(lowestScore != null ? lowestScore : 0);
        stats.setTotalScore(assignment.getTotalScore());
        
        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        
        int totalObjectiveScore = 0;
        int totalObjectiveMaxScore = 0;
        int totalSubjectiveScore = 0;
        int totalSubjectiveMaxScore = 0;
        int totalCorrectObjective = 0;
        int totalWrongObjective = 0;
        int totalObjectiveQuestions = 0;
        int totalSubjectiveQuestions = 0;
        int totalGradedSubjective = 0;
        int totalQuestions = 0;
        int totalAnswered = 0;
        
        Map<String, Integer> scoreDistribution = new LinkedHashMap<>();
        scoreDistribution.put("0-59", 0);
        scoreDistribution.put("60-69", 0);
        scoreDistribution.put("70-79", 0);
        scoreDistribution.put("80-89", 0);
        scoreDistribution.put("90-100", 0);
        
        for (Submission submission : submissions) {
            if (submission.getTotalScore() != null && assignment.getTotalScore() != null && assignment.getTotalScore() > 0) {
                int score = submission.getTotalScore();
                int maxScore = assignment.getTotalScore();
                double percentage = (double) score / maxScore * 100;
                
                if (percentage < 60) {
                    scoreDistribution.put("0-59", scoreDistribution.get("0-59") + 1);
                } else if (percentage < 70) {
                    scoreDistribution.put("60-69", scoreDistribution.get("60-69") + 1);
                } else if (percentage < 80) {
                    scoreDistribution.put("70-79", scoreDistribution.get("70-79") + 1);
                } else if (percentage < 90) {
                    scoreDistribution.put("80-89", scoreDistribution.get("80-89") + 1);
                } else {
                    scoreDistribution.put("90-100", scoreDistribution.get("90-100") + 1);
                }
            }
            
            if (submission.getObjectiveScore() != null) {
                totalObjectiveScore += submission.getObjectiveScore();
            }
            if (submission.getObjectiveMaxScore() != null) {
                totalObjectiveMaxScore += submission.getObjectiveMaxScore();
            }
            if (submission.getSubjectiveScore() != null) {
                totalSubjectiveScore += submission.getSubjectiveScore();
            }
            if (submission.getSubjectiveMaxScore() != null) {
                totalSubjectiveMaxScore += submission.getSubjectiveMaxScore();
            }
            if (submission.getCorrectObjectiveQuestions() != null) {
                totalCorrectObjective += submission.getCorrectObjectiveQuestions();
            }
            if (submission.getWrongObjectiveQuestions() != null) {
                totalWrongObjective += submission.getWrongObjectiveQuestions();
            }
            if (submission.getTotalObjectiveQuestions() != null) {
                totalObjectiveQuestions += submission.getTotalObjectiveQuestions();
            }
            if (submission.getTotalSubjectiveQuestions() != null) {
                totalSubjectiveQuestions += submission.getTotalSubjectiveQuestions();
            }
            if (submission.getGradedSubjectiveQuestions() != null) {
                totalGradedSubjective += submission.getGradedSubjectiveQuestions();
            }
            if (submission.getTotalQuestions() != null) {
                totalQuestions += submission.getTotalQuestions();
            }
            if (submission.getAnsweredQuestions() != null) {
                totalAnswered += submission.getAnsweredQuestions();
            }
        }
        
        stats.setScoreDistribution(scoreDistribution);
        stats.setTotalObjectiveScore(totalObjectiveScore);
        stats.setTotalObjectiveMaxScore(totalObjectiveMaxScore);
        stats.setTotalSubjectiveScore(totalSubjectiveScore);
        stats.setTotalSubjectiveMaxScore(totalSubjectiveMaxScore);
        stats.setTotalCorrectObjective(totalCorrectObjective);
        stats.setTotalWrongObjective(totalWrongObjective);
        stats.setTotalObjectiveQuestions(totalObjectiveQuestions);
        stats.setTotalSubjectiveQuestions(totalSubjectiveQuestions);
        stats.setTotalGradedSubjective(totalGradedSubjective);
        stats.setTotalQuestions(totalQuestions);
        stats.setTotalAnswered(totalAnswered);
        
        if (totalObjectiveMaxScore > 0) {
            stats.setAverageObjectiveAccuracy((double) totalObjectiveScore / totalObjectiveMaxScore * 100);
        }
        if (totalObjectiveQuestions > 0) {
            stats.setOverallObjectiveAccuracy((double) totalCorrectObjective / totalObjectiveQuestions * 100);
        }
        if (totalSubjectiveMaxScore > 0 && totalGradedSubjective > 0) {
            stats.setAverageSubjectiveScoreRate((double) totalSubjectiveScore / totalSubjectiveMaxScore * 100);
        }
        
        log.info("作业统计计算完成，assignmentId={}, 提交数={}, 平均分={}", 
                assignmentId, submittedCount, averageScore);
        
        return stats;
    }

    public StudentStatistics getStudentStatistics(Long studentId) {
        List<Submission> submissions = submissionRepository.findByStudentId(studentId);
        
        int totalAssignments = submissions.size();
        int submittedCount = 0;
        int gradedCount = 0;
        int totalScoreSum = 0;
        int maxScoreSum = 0;
        
        int totalObjectiveScore = 0;
        int totalObjectiveMaxScore = 0;
        int totalSubjectiveScore = 0;
        int totalSubjectiveMaxScore = 0;
        int totalCorrectObjective = 0;
        int totalWrongObjective = 0;
        int totalObjectiveQuestions = 0;
        int totalSubjectiveQuestions = 0;
        int totalQuestions = 0;
        int totalAnswered = 0;
        
        for (Submission submission : submissions) {
            submittedCount++;
            
            if (submission.getStatus() == AssignmentStatus.GRADED ||
                submission.getStatus() == AssignmentStatus.RETURNED ||
                submission.getStatus() == AssignmentStatus.ARCHIVED) {
                gradedCount++;
                if (submission.getTotalScore() != null) {
                    totalScoreSum += submission.getTotalScore();
                }
                if (submission.getAssignment() != null && submission.getAssignment().getTotalScore() != null) {
                    maxScoreSum += submission.getAssignment().getTotalScore();
                }
                
                if (submission.getObjectiveScore() != null) {
                    totalObjectiveScore += submission.getObjectiveScore();
                }
                if (submission.getObjectiveMaxScore() != null) {
                    totalObjectiveMaxScore += submission.getObjectiveMaxScore();
                }
                if (submission.getSubjectiveScore() != null) {
                    totalSubjectiveScore += submission.getSubjectiveScore();
                }
                if (submission.getSubjectiveMaxScore() != null) {
                    totalSubjectiveMaxScore += submission.getSubjectiveMaxScore();
                }
                if (submission.getCorrectObjectiveQuestions() != null) {
                    totalCorrectObjective += submission.getCorrectObjectiveQuestions();
                }
                if (submission.getWrongObjectiveQuestions() != null) {
                    totalWrongObjective += submission.getWrongObjectiveQuestions();
                }
                if (submission.getTotalObjectiveQuestions() != null) {
                    totalObjectiveQuestions += submission.getTotalObjectiveQuestions();
                }
                if (submission.getTotalSubjectiveQuestions() != null) {
                    totalSubjectiveQuestions += submission.getTotalSubjectiveQuestions();
                }
                if (submission.getTotalQuestions() != null) {
                    totalQuestions += submission.getTotalQuestions();
                }
                if (submission.getAnsweredQuestions() != null) {
                    totalAnswered += submission.getAnsweredQuestions();
                }
            }
        }
        
        Long unresolvedWrongCount = wrongQuestionRepository.countByStudentIdAndIsResolved(studentId, false);
        Long resolvedWrongCount = wrongQuestionRepository.countByStudentIdAndIsResolved(studentId, true);
        
        StudentStatistics stats = new StudentStatistics();
        stats.setStudentId(studentId);
        stats.setTotalAssignments(totalAssignments);
        stats.setSubmittedCount(submittedCount);
        stats.setGradedCount(gradedCount);
        
        if (maxScoreSum > 0) {
            stats.setAverageScore((double) totalScoreSum / maxScoreSum * 100);
        } else {
            stats.setAverageScore(0.0);
        }
        
        stats.setTotalObjectiveScore(totalObjectiveScore);
        stats.setTotalObjectiveMaxScore(totalObjectiveMaxScore);
        stats.setTotalSubjectiveScore(totalSubjectiveScore);
        stats.setTotalSubjectiveMaxScore(totalSubjectiveMaxScore);
        stats.setTotalCorrectObjective(totalCorrectObjective);
        stats.setTotalWrongObjective(totalWrongObjective);
        stats.setTotalObjectiveQuestions(totalObjectiveQuestions);
        stats.setTotalSubjectiveQuestions(totalSubjectiveQuestions);
        stats.setTotalQuestions(totalQuestions);
        stats.setTotalAnswered(totalAnswered);
        
        if (totalObjectiveMaxScore > 0) {
            stats.setAverageObjectiveAccuracy((double) totalObjectiveScore / totalObjectiveMaxScore * 100);
        }
        if (totalObjectiveQuestions > 0) {
            stats.setOverallObjectiveAccuracy((double) totalCorrectObjective / totalObjectiveQuestions * 100);
        }
        
        stats.setUnresolvedWrongQuestions(unresolvedWrongCount.intValue());
        stats.setResolvedWrongQuestions(resolvedWrongCount.intValue());
        stats.setTotalWrongQuestions((unresolvedWrongCount + resolvedWrongCount).intValue());
        
        log.info("学生统计计算完成，studentId={}, 作业数={}, 平均分={}", 
                studentId, totalAssignments, stats.getAverageScore());
        
        return stats;
    }

    public ClassStatistics getClassStatistics(Long classId) {
        List<Assignment> assignments = assignmentRepository.findByClazzId(classId);
        Long totalStudents = clazzRepository.countStudentsByClassId(classId);
        
        int totalAssignments = assignments.size();
        double totalSubmissionRate = 0.0;
        double totalAverageScore = 0.0;
        int gradedAssignments = 0;
        
        int totalObjectiveScore = 0;
        int totalObjectiveMaxScore = 0;
        int totalSubjectiveScore = 0;
        int totalSubjectiveMaxScore = 0;
        
        for (Assignment assignment : assignments) {
            Long submittedCount = submissionRepository.countByAssignmentId(assignment.getId());
            if (totalStudents > 0) {
                totalSubmissionRate += (double) submittedCount / totalStudents * 100;
            }
            
            Double avgScore = submissionRepository.getAverageScoreByAssignmentId(assignment.getId());
            if (avgScore != null) {
                totalAverageScore += avgScore;
                gradedAssignments++;
            }
            
            List<Submission> submissions = submissionRepository.findByAssignmentId(assignment.getId());
            for (Submission submission : submissions) {
                if (submission.getObjectiveScore() != null) {
                    totalObjectiveScore += submission.getObjectiveScore();
                }
                if (submission.getObjectiveMaxScore() != null) {
                    totalObjectiveMaxScore += submission.getObjectiveMaxScore();
                }
                if (submission.getSubjectiveScore() != null) {
                    totalSubjectiveScore += submission.getSubjectiveScore();
                }
                if (submission.getSubjectiveMaxScore() != null) {
                    totalSubjectiveMaxScore += submission.getSubjectiveMaxScore();
                }
            }
        }
        
        ClassStatistics stats = new ClassStatistics();
        stats.setClassId(classId);
        stats.setTotalStudents(totalStudents.intValue());
        stats.setTotalAssignments(totalAssignments);
        stats.setAverageSubmissionRate(totalAssignments > 0 ? totalSubmissionRate / totalAssignments : 0.0);
        stats.setAverageScore(gradedAssignments > 0 ? totalAverageScore / gradedAssignments : 0.0);
        
        stats.setTotalObjectiveScore(totalObjectiveScore);
        stats.setTotalObjectiveMaxScore(totalObjectiveMaxScore);
        stats.setTotalSubjectiveScore(totalSubjectiveScore);
        stats.setTotalSubjectiveMaxScore(totalSubjectiveMaxScore);
        
        if (totalObjectiveMaxScore > 0) {
            stats.setAverageObjectiveAccuracy((double) totalObjectiveScore / totalObjectiveMaxScore * 100);
        }
        if (totalSubjectiveMaxScore > 0) {
            stats.setAverageSubjectiveScoreRate((double) totalSubjectiveScore / totalSubjectiveMaxScore * 100);
        }
        
        return stats;
    }

    public List<KnowledgePointStatistics> getKnowledgePointStatistics(Long studentId) {
        List<Object[]> results = wrongQuestionRepository.countWrongQuestionsByKnowledgePoint(studentId);
        List<KnowledgePointStatistics> stats = new ArrayList<>();
        
        for (Object[] result : results) {
            String knowledgePoint = (String) result[0];
            Long count = (Long) result[1];
            
            KnowledgePointStatistics kps = new KnowledgePointStatistics();
            kps.setKnowledgePoint(knowledgePoint != null ? knowledgePoint : "未分类");
            kps.setWrongCount(count.intValue());
            stats.add(kps);
        }
        
        return stats;
    }

    public static class AssignmentStatistics {
        private Long assignmentId;
        private String assignmentTitle;
        private Integer totalStudents;
        private Integer submittedCount;
        private Integer unsubmittedCount;
        private Double submissionRate;
        private Double averageScore;
        private Integer highestScore;
        private Integer lowestScore;
        private Integer totalScore;
        private Map<String, Integer> scoreDistribution;
        
        private Integer totalObjectiveScore;
        private Integer totalObjectiveMaxScore;
        private Integer totalSubjectiveScore;
        private Integer totalSubjectiveMaxScore;
        private Integer totalCorrectObjective;
        private Integer totalWrongObjective;
        private Integer totalObjectiveQuestions;
        private Integer totalSubjectiveQuestions;
        private Integer totalGradedSubjective;
        private Integer totalQuestions;
        private Integer totalAnswered;
        
        private Double averageObjectiveAccuracy;
        private Double overallObjectiveAccuracy;
        private Double averageSubjectiveScoreRate;

        public Long getAssignmentId() { return assignmentId; }
        public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
        public String getAssignmentTitle() { return assignmentTitle; }
        public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }
        public Integer getTotalStudents() { return totalStudents; }
        public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }
        public Integer getSubmittedCount() { return submittedCount; }
        public void setSubmittedCount(Integer submittedCount) { this.submittedCount = submittedCount; }
        public Integer getUnsubmittedCount() { return unsubmittedCount; }
        public void setUnsubmittedCount(Integer unsubmittedCount) { this.unsubmittedCount = unsubmittedCount; }
        public Double getSubmissionRate() { return submissionRate; }
        public void setSubmissionRate(Double submissionRate) { this.submissionRate = submissionRate; }
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
        public Integer getHighestScore() { return highestScore; }
        public void setHighestScore(Integer highestScore) { this.highestScore = highestScore; }
        public Integer getLowestScore() { return lowestScore; }
        public void setLowestScore(Integer lowestScore) { this.lowestScore = lowestScore; }
        public Integer getTotalScore() { return totalScore; }
        public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
        public Map<String, Integer> getScoreDistribution() { return scoreDistribution; }
        public void setScoreDistribution(Map<String, Integer> scoreDistribution) { this.scoreDistribution = scoreDistribution; }
        public Integer getTotalObjectiveScore() { return totalObjectiveScore; }
        public void setTotalObjectiveScore(Integer totalObjectiveScore) { this.totalObjectiveScore = totalObjectiveScore; }
        public Integer getTotalObjectiveMaxScore() { return totalObjectiveMaxScore; }
        public void setTotalObjectiveMaxScore(Integer totalObjectiveMaxScore) { this.totalObjectiveMaxScore = totalObjectiveMaxScore; }
        public Integer getTotalSubjectiveScore() { return totalSubjectiveScore; }
        public void setTotalSubjectiveScore(Integer totalSubjectiveScore) { this.totalSubjectiveScore = totalSubjectiveScore; }
        public Integer getTotalSubjectiveMaxScore() { return totalSubjectiveMaxScore; }
        public void setTotalSubjectiveMaxScore(Integer totalSubjectiveMaxScore) { this.totalSubjectiveMaxScore = totalSubjectiveMaxScore; }
        public Integer getTotalCorrectObjective() { return totalCorrectObjective; }
        public void setTotalCorrectObjective(Integer totalCorrectObjective) { this.totalCorrectObjective = totalCorrectObjective; }
        public Integer getTotalWrongObjective() { return totalWrongObjective; }
        public void setTotalWrongObjective(Integer totalWrongObjective) { this.totalWrongObjective = totalWrongObjective; }
        public Integer getTotalObjectiveQuestions() { return totalObjectiveQuestions; }
        public void setTotalObjectiveQuestions(Integer totalObjectiveQuestions) { this.totalObjectiveQuestions = totalObjectiveQuestions; }
        public Integer getTotalSubjectiveQuestions() { return totalSubjectiveQuestions; }
        public void setTotalSubjectiveQuestions(Integer totalSubjectiveQuestions) { this.totalSubjectiveQuestions = totalSubjectiveQuestions; }
        public Integer getTotalGradedSubjective() { return totalGradedSubjective; }
        public void setTotalGradedSubjective(Integer totalGradedSubjective) { this.totalGradedSubjective = totalGradedSubjective; }
        public Integer getTotalQuestions() { return totalQuestions; }
        public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
        public Integer getTotalAnswered() { return totalAnswered; }
        public void setTotalAnswered(Integer totalAnswered) { this.totalAnswered = totalAnswered; }
        public Double getAverageObjectiveAccuracy() { return averageObjectiveAccuracy; }
        public void setAverageObjectiveAccuracy(Double averageObjectiveAccuracy) { this.averageObjectiveAccuracy = averageObjectiveAccuracy; }
        public Double getOverallObjectiveAccuracy() { return overallObjectiveAccuracy; }
        public void setOverallObjectiveAccuracy(Double overallObjectiveAccuracy) { this.overallObjectiveAccuracy = overallObjectiveAccuracy; }
        public Double getAverageSubjectiveScoreRate() { return averageSubjectiveScoreRate; }
        public void setAverageSubjectiveScoreRate(Double averageSubjectiveScoreRate) { this.averageSubjectiveScoreRate = averageSubjectiveScoreRate; }
    }

    public static class StudentStatistics {
        private Long studentId;
        private Integer totalAssignments;
        private Integer submittedCount;
        private Integer gradedCount;
        private Double averageScore;
        
        private Integer totalObjectiveScore;
        private Integer totalObjectiveMaxScore;
        private Integer totalSubjectiveScore;
        private Integer totalSubjectiveMaxScore;
        private Integer totalCorrectObjective;
        private Integer totalWrongObjective;
        private Integer totalObjectiveQuestions;
        private Integer totalSubjectiveQuestions;
        private Integer totalQuestions;
        private Integer totalAnswered;
        
        private Double averageObjectiveAccuracy;
        private Double overallObjectiveAccuracy;
        
        private Integer unresolvedWrongQuestions;
        private Integer resolvedWrongQuestions;
        private Integer totalWrongQuestions;

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public Integer getTotalAssignments() { return totalAssignments; }
        public void setTotalAssignments(Integer totalAssignments) { this.totalAssignments = totalAssignments; }
        public Integer getSubmittedCount() { return submittedCount; }
        public void setSubmittedCount(Integer submittedCount) { this.submittedCount = submittedCount; }
        public Integer getGradedCount() { return gradedCount; }
        public void setGradedCount(Integer gradedCount) { this.gradedCount = gradedCount; }
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
        public Integer getTotalObjectiveScore() { return totalObjectiveScore; }
        public void setTotalObjectiveScore(Integer totalObjectiveScore) { this.totalObjectiveScore = totalObjectiveScore; }
        public Integer getTotalObjectiveMaxScore() { return totalObjectiveMaxScore; }
        public void setTotalObjectiveMaxScore(Integer totalObjectiveMaxScore) { this.totalObjectiveMaxScore = totalObjectiveMaxScore; }
        public Integer getTotalSubjectiveScore() { return totalSubjectiveScore; }
        public void setTotalSubjectiveScore(Integer totalSubjectiveScore) { this.totalSubjectiveScore = totalSubjectiveScore; }
        public Integer getTotalSubjectiveMaxScore() { return totalSubjectiveMaxScore; }
        public void setTotalSubjectiveMaxScore(Integer totalSubjectiveMaxScore) { this.totalSubjectiveMaxScore = totalSubjectiveMaxScore; }
        public Integer getTotalCorrectObjective() { return totalCorrectObjective; }
        public void setTotalCorrectObjective(Integer totalCorrectObjective) { this.totalCorrectObjective = totalCorrectObjective; }
        public Integer getTotalWrongObjective() { return totalWrongObjective; }
        public void setTotalWrongObjective(Integer totalWrongObjective) { this.totalWrongObjective = totalWrongObjective; }
        public Integer getTotalObjectiveQuestions() { return totalObjectiveQuestions; }
        public void setTotalObjectiveQuestions(Integer totalObjectiveQuestions) { this.totalObjectiveQuestions = totalObjectiveQuestions; }
        public Integer getTotalSubjectiveQuestions() { return totalSubjectiveQuestions; }
        public void setTotalSubjectiveQuestions(Integer totalSubjectiveQuestions) { this.totalSubjectiveQuestions = totalSubjectiveQuestions; }
        public Integer getTotalQuestions() { return totalQuestions; }
        public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
        public Integer getTotalAnswered() { return totalAnswered; }
        public void setTotalAnswered(Integer totalAnswered) { this.totalAnswered = totalAnswered; }
        public Double getAverageObjectiveAccuracy() { return averageObjectiveAccuracy; }
        public void setAverageObjectiveAccuracy(Double averageObjectiveAccuracy) { this.averageObjectiveAccuracy = averageObjectiveAccuracy; }
        public Double getOverallObjectiveAccuracy() { return overallObjectiveAccuracy; }
        public void setOverallObjectiveAccuracy(Double overallObjectiveAccuracy) { this.overallObjectiveAccuracy = overallObjectiveAccuracy; }
        public Integer getUnresolvedWrongQuestions() { return unresolvedWrongQuestions; }
        public void setUnresolvedWrongQuestions(Integer unresolvedWrongQuestions) { this.unresolvedWrongQuestions = unresolvedWrongQuestions; }
        public Integer getResolvedWrongQuestions() { return resolvedWrongQuestions; }
        public void setResolvedWrongQuestions(Integer resolvedWrongQuestions) { this.resolvedWrongQuestions = resolvedWrongQuestions; }
        public Integer getTotalWrongQuestions() { return totalWrongQuestions; }
        public void setTotalWrongQuestions(Integer totalWrongQuestions) { this.totalWrongQuestions = totalWrongQuestions; }
    }

    public static class ClassStatistics {
        private Long classId;
        private Integer totalStudents;
        private Integer totalAssignments;
        private Double averageSubmissionRate;
        private Double averageScore;
        
        private Integer totalObjectiveScore;
        private Integer totalObjectiveMaxScore;
        private Integer totalSubjectiveScore;
        private Integer totalSubjectiveMaxScore;
        private Double averageObjectiveAccuracy;
        private Double averageSubjectiveScoreRate;

        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }
        public Integer getTotalStudents() { return totalStudents; }
        public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }
        public Integer getTotalAssignments() { return totalAssignments; }
        public void setTotalAssignments(Integer totalAssignments) { this.totalAssignments = totalAssignments; }
        public Double getAverageSubmissionRate() { return averageSubmissionRate; }
        public void setAverageSubmissionRate(Double averageSubmissionRate) { this.averageSubmissionRate = averageSubmissionRate; }
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
        public Integer getTotalObjectiveScore() { return totalObjectiveScore; }
        public void setTotalObjectiveScore(Integer totalObjectiveScore) { this.totalObjectiveScore = totalObjectiveScore; }
        public Integer getTotalObjectiveMaxScore() { return totalObjectiveMaxScore; }
        public void setTotalObjectiveMaxScore(Integer totalObjectiveMaxScore) { this.totalObjectiveMaxScore = totalObjectiveMaxScore; }
        public Integer getTotalSubjectiveScore() { return totalSubjectiveScore; }
        public void setTotalSubjectiveScore(Integer totalSubjectiveScore) { this.totalSubjectiveScore = totalSubjectiveScore; }
        public Integer getTotalSubjectiveMaxScore() { return totalSubjectiveMaxScore; }
        public void setTotalSubjectiveMaxScore(Integer totalSubjectiveMaxScore) { this.totalSubjectiveMaxScore = totalSubjectiveMaxScore; }
        public Double getAverageObjectiveAccuracy() { return averageObjectiveAccuracy; }
        public void setAverageObjectiveAccuracy(Double averageObjectiveAccuracy) { this.averageObjectiveAccuracy = averageObjectiveAccuracy; }
        public Double getAverageSubjectiveScoreRate() { return averageSubjectiveScoreRate; }
        public void setAverageSubjectiveScoreRate(Double averageSubjectiveScoreRate) { this.averageSubjectiveScoreRate = averageSubjectiveScoreRate; }
    }

    public static class KnowledgePointStatistics {
        private String knowledgePoint;
        private Integer wrongCount;

        public String getKnowledgePoint() { return knowledgePoint; }
        public void setKnowledgePoint(String knowledgePoint) { this.knowledgePoint = knowledgePoint; }
        public Integer getWrongCount() { return wrongCount; }
        public void setWrongCount(Integer wrongCount) { this.wrongCount = wrongCount; }
    }
}
