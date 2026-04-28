package com.education.assignment.service;

import com.education.assignment.entity.LearningReport;
import com.education.assignment.entity.Submission;
import com.education.assignment.entity.User;
import com.education.assignment.enums.AssignmentStatus;
import com.education.assignment.repository.LearningReportRepository;
import com.education.assignment.repository.SubmissionRepository;
import com.education.assignment.repository.UserRepository;
import com.education.assignment.repository.WrongQuestionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningReportService {
    private final LearningReportRepository learningReportRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final WrongQuestionRepository wrongQuestionRepository;

    @Transactional
    public LearningReport generateReport(Long studentId, LearningReport.ReportType reportType) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = calculateStartDate(reportType, endDate);
        
        List<Submission> submissions = submissionRepository.findByStudentId(studentId);
        
        int totalAssignments = 0;
        int submittedCount = 0;
        int gradedCount = 0;
        int totalScoreSum = 0;
        int maxScoreSum = 0;
        int totalQuestions = 0;
        int correctQuestions = 0;
        int wrongQuestions = 0;
        
        for (Submission submission : submissions) {
            if (submission.getSubmittedAt() != null &&
                submission.getSubmittedAt().isAfter(startDate) &&
                submission.getSubmittedAt().isBefore(endDate)) {
                totalAssignments++;
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
                }
            }
        }
        
        Long unresolvedWrongCount = wrongQuestionRepository.countByStudentIdAndIsResolved(studentId, false);
        wrongQuestions = unresolvedWrongCount.intValue();
        
        LearningReport report = new LearningReport();
        report.setStudent(student);
        report.setReportType(reportType);
        report.setTitle(generateReportTitle(reportType));
        report.setReportPeriodStart(startDate);
        report.setReportPeriodEnd(endDate);
        report.setTotalAssignments(totalAssignments);
        report.setSubmittedAssignments(submittedCount);
        report.setGradedAssignments(gradedCount);
        
        if (maxScoreSum > 0) {
            report.setAverageScore((double) totalScoreSum / maxScoreSum * 100);
        } else {
            report.setAverageScore(0.0);
        }
        
        report.setTotalQuestions(totalQuestions);
        report.setCorrectQuestions(correctQuestions);
        report.setWrongQuestions(wrongQuestions);
        
        if (totalQuestions > 0) {
            report.setAccuracyRate((double) correctQuestions / totalQuestions * 100);
        } else {
            report.setAccuracyRate(0.0);
        }
        
        report.setSummary(generateSummary(report));
        report.setKnowledgePointAnalysis(generateKnowledgeAnalysis(studentId));
        report.setRecommendations(generateRecommendations(report));
        report.setGeneratedAt(LocalDateTime.now());
        
        return learningReportRepository.save(report);
    }

    private LocalDateTime calculateStartDate(LearningReport.ReportType type, LocalDateTime endDate) {
        switch (type) {
            case DAILY:
                return endDate.minusDays(1);
            case WEEKLY:
                return endDate.minusWeeks(1);
            case MONTHLY:
                return endDate.minusMonths(1);
            case CUSTOM:
            default:
                return endDate.minusMonths(1);
        }
    }

    private String generateReportTitle(LearningReport.ReportType type) {
        switch (type) {
            case DAILY:
                return "每日学习报告";
            case WEEKLY:
                return "每周学习报告";
            case MONTHLY:
                return "每月学习报告";
            case CUSTOM:
            default:
                return "学习报告";
        }
    }

    private String generateSummary(LearningReport report) {
        StringBuilder summary = new StringBuilder();
        summary.append("报告期间：完成了 ").append(report.getSubmittedAssignments()).append(" 份作业。");
        summary.append("平均得分：").append(String.format("%.1f", report.getAverageScore())).append("分。");
        summary.append("未解决的错题：").append(report.getWrongQuestions()).append(" 道。");
        return summary.toString();
    }

    private String generateKnowledgeAnalysis(Long studentId) {
        List<Object[]> results = wrongQuestionRepository.countWrongQuestionsByKnowledgePoint(studentId);
        if (results.isEmpty()) {
            return "暂无知识点分析数据。";
        }
        
        StringBuilder analysis = new StringBuilder("知识点掌握情况分析：\n");
        for (Object[] result : results) {
            String kp = (String) result[0];
            Long count = (Long) result[1];
            if (kp != null && !kp.isEmpty()) {
                analysis.append("- ").append(kp).append("：错误 ").append(count).append(" 次\n");
            }
        }
        return analysis.toString();
    }

    private String generateRecommendations(LearningReport report) {
        StringBuilder recommendations = new StringBuilder();
        
        if (report.getAverageScore() < 60) {
            recommendations.append("建议：巩固基础知识，多做练习题。\n");
        } else if (report.getAverageScore() < 80) {
            recommendations.append("建议：继续努力，重点攻克薄弱知识点。\n");
        } else {
            recommendations.append("建议：继续保持，可以挑战更难的题目。\n");
        }
        
        if (report.getWrongQuestions() > 0) {
            recommendations.append("错题提醒：您有 ").append(report.getWrongQuestions())
                    .append(" 道未解决的错题，建议及时复习。\n");
        }
        
        return recommendations.toString();
    }

    public List<LearningReport> getReportsByStudentId(Long studentId) {
        return learningReportRepository.findByStudentIdOrderByGeneratedAtDesc(studentId);
    }

    public LearningReport getReportById(Long id) {
        return learningReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("报告不存在"));
    }

    public LearningReport getLatestReport(Long studentId) {
        return learningReportRepository.findLatestByStudentId(studentId);
    }

    @Transactional
    public void deleteReport(Long id) {
        LearningReport report = learningReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("报告不存在"));
        learningReportRepository.delete(report);
    }
}
