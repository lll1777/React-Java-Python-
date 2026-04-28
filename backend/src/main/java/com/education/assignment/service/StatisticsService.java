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
import org.springframework.stereotype.Service;

import java.util.*;

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
        stats.setSubmissionRate(submittedCount.doubleValue() / totalStudents.doubleValue() * 100);
        stats.setAverageScore(averageScore != null ? averageScore : 0.0);
        stats.setHighestScore(highestScore != null ? highestScore : 0);
        stats.setLowestScore(lowestScore != null ? lowestScore : 0);
        stats.setTotalScore(assignment.getTotalScore());
        
        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        Map<String, Integer> scoreDistribution = new LinkedHashMap<>();
        scoreDistribution.put("0-59", 0);
        scoreDistribution.put("60-69", 0);
        scoreDistribution.put("70-79", 0);
        scoreDistribution.put("80-89", 0);
        scoreDistribution.put("90-100", 0);
        
        for (Submission submission : submissions) {
            if (submission.getTotalScore() != null) {
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
        }
        stats.setScoreDistribution(scoreDistribution);
        
        return stats;
    }

    public StudentStatistics getStudentStatistics(Long studentId) {
        List<Submission> submissions = submissionRepository.findByStudentId(studentId);
        
        int totalAssignments = submissions.size();
        int gradedCount = 0;
        int totalScoreSum = 0;
        int maxScoreSum = 0;
        
        for (Submission submission : submissions) {
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
            }
        }
        
        Long unresolvedWrongCount = wrongQuestionRepository.countByStudentIdAndIsResolved(studentId, false);
        Long resolvedWrongCount = wrongQuestionRepository.countByStudentIdAndIsResolved(studentId, true);
        
        StudentStatistics stats = new StudentStatistics();
        stats.setStudentId(studentId);
        stats.setTotalAssignments(totalAssignments);
        stats.setSubmittedCount(totalAssignments);
        stats.setGradedCount(gradedCount);
        stats.setAverageScore(maxScoreSum > 0 ? (double) totalScoreSum / maxScoreSum * 100 : 0.0);
        stats.setUnresolvedWrongQuestions(unresolvedWrongCount.intValue());
        stats.setResolvedWrongQuestions(resolvedWrongCount.intValue());
        stats.setTotalWrongQuestions((unresolvedWrongCount + resolvedWrongCount).intValue());
        
        return stats;
    }

    public ClassStatistics getClassStatistics(Long classId) {
        List<Assignment> assignments = assignmentRepository.findByClazzId(classId);
        Long totalStudents = clazzRepository.countStudentsByClassId(classId);
        
        int totalAssignments = assignments.size();
        double totalSubmissionRate = 0.0;
        double totalAverageScore = 0.0;
        int gradedAssignments = 0;
        
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
        }
        
        ClassStatistics stats = new ClassStatistics();
        stats.setClassId(classId);
        stats.setTotalStudents(totalStudents.intValue());
        stats.setTotalAssignments(totalAssignments);
        stats.setAverageSubmissionRate(totalAssignments > 0 ? totalSubmissionRate / totalAssignments : 0.0);
        stats.setAverageScore(gradedAssignments > 0 ? totalAverageScore / gradedAssignments : 0.0);
        
        return stats;
    }

    public List<KnowledgePointStatistics> getKnowledgePointStatistics(Long studentId) {
        List<Object[]> results = wrongQuestionRepository.countWrongQuestionsByKnowledgePoint(studentId);
        List<KnowledgePointStatistics> stats = new ArrayList<>();
        
        for (Object[] result : results) {
            String knowledgePoint = (String) result[0];
            Long count = (Long) result[1];
            
            KnowledgePointStatistics kps = new KnowledgePointStatistics();
            kps.setKnowledgePoint(knowledgePoint);
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
    }

    public static class StudentStatistics {
        private Long studentId;
        private Integer totalAssignments;
        private Integer submittedCount;
        private Integer gradedCount;
        private Double averageScore;
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
