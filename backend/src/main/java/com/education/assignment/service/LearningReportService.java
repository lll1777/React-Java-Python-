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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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
            }
        }
        
        Long unresolvedWrongCount = wrongQuestionRepository.countByStudentIdAndIsResolved(studentId, false);
        Long resolvedWrongCount = wrongQuestionRepository.countByStudentIdAndIsResolved(studentId, true);
        
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
        report.setCorrectQuestions(totalCorrectObjective);
        report.setWrongQuestions(totalWrongObjective);
        report.setUnresolvedWrongQuestions(unresolvedWrongCount.intValue());
        report.setResolvedWrongQuestions(resolvedWrongCount.intValue());
        
        report.setTotalObjectiveScore(totalObjectiveScore);
        report.setTotalObjectiveMaxScore(totalObjectiveMaxScore);
        report.setTotalSubjectiveScore(totalSubjectiveScore);
        report.setTotalSubjectiveMaxScore(totalSubjectiveMaxScore);
        report.setTotalObjectiveQuestions(totalObjectiveQuestions);
        report.setTotalSubjectiveQuestions(totalSubjectiveQuestions);
        report.setTotalGradedSubjective(totalGradedSubjective);
        
        if (totalObjectiveMaxScore > 0) {
            report.setAverageObjectiveAccuracy((double) totalObjectiveScore / totalObjectiveMaxScore * 100);
        } else {
            report.setAverageObjectiveAccuracy(0.0);
        }
        
        if (totalObjectiveQuestions > 0) {
            report.setOverallObjectiveAccuracy((double) totalCorrectObjective / totalObjectiveQuestions * 100);
        } else {
            report.setOverallObjectiveAccuracy(0.0);
        }
        
        if (totalSubjectiveMaxScore > 0 && totalGradedSubjective > 0) {
            report.setAverageSubjectiveScoreRate((double) totalSubjectiveScore / totalSubjectiveMaxScore * 100);
        } else {
            report.setAverageSubjectiveScoreRate(0.0);
        }
        
        if (totalQuestions > 0) {
            report.setAccuracyRate((double) totalCorrectObjective / totalQuestions * 100);
        } else {
            report.setAccuracyRate(0.0);
        }
        
        report.setSummary(generateSummary(report));
        report.setKnowledgePointAnalysis(generateKnowledgeAnalysis(studentId));
        report.setRecommendations(generateRecommendations(report));
        report.setGeneratedAt(LocalDateTime.now());
        
        log.info("学习报告生成完成，studentId={}, 类型={}, 平均分={}", 
                studentId, reportType, report.getAverageScore());
        
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
        summary.append("【学习概况】\n");
        summary.append("报告期间：完成了 ").append(report.getSubmittedAssignments()).append(" 份作业，已批改 ").append(report.getGradedAssignments()).append(" 份。\n");
        summary.append("平均得分：").append(String.format("%.1f", report.getAverageScore())).append("分。\n\n");
        
        summary.append("【客观题统计】\n");
        summary.append("总题数：").append(report.getTotalObjectiveQuestions()).append(" 道\n");
        summary.append("正确：").append(report.getCorrectQuestions()).append(" 道，错误：").append(report.getWrongQuestions()).append(" 道\n");
        summary.append("得分：").append(report.getTotalObjectiveScore()).append("/").append(report.getTotalObjectiveMaxScore()).append("\n");
        summary.append("正确率：").append(String.format("%.1f", report.getOverallObjectiveAccuracy())).append("%\n\n");
        
        summary.append("【主观题统计】\n");
        summary.append("总题数：").append(report.getTotalSubjectiveQuestions()).append(" 道\n");
        summary.append("已批改：").append(report.getTotalGradedSubjective()).append(" 道\n");
        summary.append("得分：").append(report.getTotalSubjectiveScore()).append("/").append(report.getTotalSubjectiveMaxScore()).append("\n");
        summary.append("得分率：").append(String.format("%.1f", report.getAverageSubjectiveScoreRate())).append("%\n\n");
        
        summary.append("【错题情况】\n");
        summary.append("未解决错题：").append(report.getUnresolvedWrongQuestions()).append(" 道\n");
        summary.append("已解决错题：").append(report.getResolvedWrongQuestions()).append(" 道");
        
        return summary.toString();
    }

    private String generateKnowledgeAnalysis(Long studentId) {
        List<Object[]> results = wrongQuestionRepository.countWrongQuestionsByKnowledgePoint(studentId);
        if (results.isEmpty()) {
            return "暂无知识点分析数据。您的表现非常出色，没有错题记录！";
        }
        
        StringBuilder analysis = new StringBuilder("【知识点掌握情况分析】\n\n");
        
        int totalWrong = 0;
        for (Object[] result : results) {
            Long count = (Long) result[1];
            totalWrong += count;
        }
        
        analysis.append("总计知识点错题：").append(totalWrong).append(" 次\n\n");
        
        for (Object[] result : results) {
            String kp = (String) result[0];
            Long count = (Long) result[1];
            
            if (kp != null && !kp.isEmpty() && count > 0) {
                double percentage = totalWrong > 0 ? (double) count / totalWrong * 100 : 0;
                analysis.append("● ").append(kp).append("：错误 ").append(count).append(" 次 (占比 ").append(String.format("%.1f", percentage)).append("%)\n");
            }
        }
        
        analysis.append("\n【薄弱知识点建议】\n");
        analysis.append("请重点复习以上错误次数较多的知识点，多做相关练习题以巩固理解。");
        
        return analysis.toString();
    }

    private String generateRecommendations(LearningReport report) {
        StringBuilder recommendations = new StringBuilder("【个性化学习建议】\n\n");
        
        double avgScore = report.getAverageScore();
        double objAccuracy = report.getOverallObjectiveAccuracy();
        double subjRate = report.getAverageSubjectiveScoreRate();
        
        if (avgScore < 60) {
            recommendations.append("📚 总体建议：目前成绩偏低，建议优先巩固基础知识。\n");
            recommendations.append("   - 先完成所有作业的客观题部分，确保基础分不丢\n");
            recommendations.append("   - 主观题可以先尝试回答，再对照参考答案学习\n");
        } else if (avgScore < 80) {
            recommendations.append("📚 总体建议：成绩良好，但仍有提升空间。\n");
            recommendations.append("   - 重点攻克薄弱知识点，争取全面提高\n");
            recommendations.append("   - 主观题部分需要加强练习\n");
        } else {
            recommendations.append("📚 总体建议：成绩优秀，继续保持！\n");
            recommendations.append("   - 可以挑战更难的题目，拓展知识面\n");
            recommendations.append("   - 帮助其他同学，教学相长\n");
        }
        
        recommendations.append("\n");
        
        if (objAccuracy < 70) {
            recommendations.append("✅ 客观题建议：正确率有待提高。\n");
            recommendations.append("   - 做题时仔细审题，避免粗心错误\n");
            recommendations.append("   - 建立错题本，定期复习错题\n");
        } else if (objAccuracy < 90) {
            recommendations.append("✅ 客观题建议：正确率良好。\n");
            recommendations.append("   - 保持现有水平，争取更高正确率\n");
        } else {
            recommendations.append("✅ 客观题建议：正确率优秀！\n");
            recommendations.append("   - 继续保持，可以挑战难度更高的题目\n");
        }
        
        recommendations.append("\n");
        
        if (report.getTotalSubjectiveQuestions() > 0) {
            if (subjRate < 60) {
                recommendations.append("✍️ 主观题建议：得分率较低。\n");
                recommendations.append("   - 认真阅读题目要求，理解答题要点\n");
                recommendations.append("   - 参考答案的答题思路，学习答题技巧\n");
            } else if (subjRate < 80) {
                recommendations.append("✍️ 主观题建议：得分率良好。\n");
                recommendations.append("   - 继续练习，注意答题的完整性\n");
            } else {
                recommendations.append("✍️ 主观题建议：得分率优秀！\n");
                recommendations.append("   - 保持答题思路清晰，内容完整\n");
            }
        }
        
        recommendations.append("\n");
        
        if (report.getUnresolvedWrongQuestions() > 0) {
            recommendations.append("⚠️ 错题提醒：您有 ").append(report.getUnresolvedWrongQuestions())
                    .append(" 道未解决的错题。\n");
            recommendations.append("   - 建议及时复习错题，理解错误原因\n");
            recommendations.append("   - 可以重做一遍错题，检查是否真正掌握\n");
            recommendations.append("   - 错题解决后记得标记为已解决\n");
        } else {
            recommendations.append("🎉 恭喜！您没有未解决的错题，继续保持！\n");
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
