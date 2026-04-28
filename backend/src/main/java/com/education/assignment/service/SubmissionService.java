package com.education.assignment.service;

import com.education.assignment.dto.SubmissionCreateDTO;
import com.education.assignment.entity.*;
import com.education.assignment.enums.AssignmentStatus;
import com.education.assignment.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final SubmissionVersionRepository submissionVersionRepository;
    private final SubmissionAnswerRepository submissionAnswerRepository;
    private final QuestionRepository questionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final ScoreCalculationService scoreCalculationService;

    @Transactional
    public Submission submitAssignment(SubmissionCreateDTO dto, Long studentId, String versionNote) {
        log.info("学生 {} 提交作业，assignmentId={}", studentId, dto.getAssignmentId());
        
        Assignment assignment = assignmentRepository.findById(dto.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("作业不存在"));
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        List<Question> questions = questionRepository.findByAssignmentIdOrderByOrderIndexAsc(assignment.getId());
        
        Optional<Submission> existingSubmission = submissionRepository.findByAssignmentAndStudent(assignment, student);
        
        Submission submission;
        int newVersionNumber;
        
        if (existingSubmission.isPresent()) {
            submission = existingSubmission.get();
            log.info("发现已有提交，创建新版本。当前版本号：{}", submission.getVersionNumber());
            
            createVersionFromSubmission(submission, false);
            
            Integer maxVersion = submissionVersionRepository.findMaxVersionNumberBySubmissionId(submission.getId());
            newVersionNumber = (maxVersion != null ? maxVersion : submission.getVersionNumber()) + 1;
            
            submissionAnswerRepository.deleteAll(submission.getAnswers());
            submission.getAnswers().clear();
            
        } else {
            submission = new Submission();
            submission.setAssignment(assignment);
            submission.setStudent(student);
            newVersionNumber = 1;
        }
        
        submission.setVersionNumber(newVersionNumber);
        submission.setVersionNote(versionNote);
        submission.setStatus(AssignmentStatus.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setIsLate(checkIsLate(assignment));
        
        submission.setAutoScore(0);
        submission.setManualScore(0);
        submission.setTotalScore(0);
        
        Map<Long, Question> questionMap = new HashMap<>();
        for (Question q : questions) {
            questionMap.put(q.getId(), q);
        }
        
        List<SubmissionAnswer> answers = new ArrayList<>();
        if (dto.getAnswers() != null) {
            for (SubmissionCreateDTO.AnswerCreateDTO aDto : dto.getAnswers()) {
                Question question = questionMap.get(aDto.getQuestionId());
                if (question != null) {
                    SubmissionAnswer answer = new SubmissionAnswer();
                    answer.setSubmission(submission);
                    answer.setQuestion(question);
                    answer.setStudentAnswer(aDto.getStudentAnswer());
                    answers.add(answer);
                }
            }
        }
        
        submission.setAnswers(answers);
        
        ScoreCalculationService.SubmissionScoreResult initialResult = 
                scoreCalculationService.recalculateAndFixScores(questions, answers);
        scoreCalculationService.applyScoresToSubmission(submission, initialResult);
        
        Submission savedSubmission = submissionRepository.save(submission);
        submissionAnswerRepository.saveAll(answers);
        
        createVersionFromSubmission(savedSubmission, true);
        
        log.info("作业提交完成，submissionId={}, versionNumber={}", savedSubmission.getId(), newVersionNumber);
        
        return savedSubmission;
    }

    private boolean checkIsLate(Assignment assignment) {
        if (assignment.getDeadline() == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(assignment.getDeadline());
    }

    @Transactional
    public SubmissionVersion createVersionFromSubmission(Submission submission, boolean isLatest) {
        log.debug("为提交 {} 创建版本存档，isLatest={}", submission.getId(), isLatest);
        
        if (!isLatest) {
            Optional<SubmissionVersion> currentLatest = 
                    submissionVersionRepository.findBySubmissionIdAndIsLatestTrue(submission.getId());
            if (currentLatest.isPresent()) {
                SubmissionVersion latestVersion = currentLatest.get();
                latestVersion.setIsLatest(false);
                submissionVersionRepository.save(latestVersion);
                log.debug("更新原最新版本 {} 为非最新", latestVersion.getId());
            }
        }
        
        SubmissionVersion version = new SubmissionVersion();
        version.setSubmission(submission);
        version.setVersionNumber(submission.getVersionNumber());
        version.setVersionNote(submission.getVersionNote());
        version.setStatus(submission.getStatus());
        version.setSubmittedAt(submission.getSubmittedAt() != null ? submission.getSubmittedAt() : LocalDateTime.now());
        version.setAutoGradedAt(submission.getAutoGradedAt());
        version.setManuallyGradedAt(submission.getManuallyGradedAt());
        version.setReturnedAt(submission.getReturnedAt());
        version.setTeacherComments(submission.getTeacherComments());
        version.setIsLate(submission.getIsLate());
        version.setIsLatest(isLatest);
        
        ScoreCalculationService.TypeScoreSummary dummySummary = new ScoreCalculationService.TypeScoreSummary();
        
        version.setAutoScore(safeInt(submission.getAutoScore()));
        version.setManualScore(safeInt(submission.getManualScore()));
        version.setTotalScore(safeInt(submission.getTotalScore()));
        version.setObjectiveScore(safeInt(submission.getObjectiveScore()));
        version.setObjectiveMaxScore(safeInt(submission.getObjectiveMaxScore()));
        version.setSubjectiveScore(safeInt(submission.getSubjectiveScore()));
        version.setSubjectiveMaxScore(safeInt(submission.getSubjectiveMaxScore()));
        version.setAssignmentTotalScore(safeInt(submission.getAssignmentTotalScore()));
        version.setObjectiveAccuracy(submission.getObjectiveAccuracy());
        version.setSubjectiveScoreRate(submission.getSubjectiveScoreRate());
        version.setTotalScoreRate(submission.getTotalScoreRate());
        version.setTotalObjectiveQuestions(safeInt(submission.getTotalObjectiveQuestions()));
        version.setCorrectObjectiveQuestions(safeInt(submission.getCorrectObjectiveQuestions()));
        version.setWrongObjectiveQuestions(safeInt(submission.getWrongObjectiveQuestions()));
        version.setTotalSubjectiveQuestions(safeInt(submission.getTotalSubjectiveQuestions()));
        version.setGradedSubjectiveQuestions(safeInt(submission.getGradedSubjectiveQuestions()));
        version.setTotalQuestions(safeInt(submission.getTotalQuestions()));
        version.setAnsweredQuestions(safeInt(submission.getAnsweredQuestions()));
        
        version.setSingleChoiceScore(safeInt(submission.getSingleChoiceScore()));
        version.setSingleChoiceMaxScore(safeInt(submission.getSingleChoiceMaxScore()));
        version.setSingleChoiceCorrect(safeInt(submission.getSingleChoiceCorrect()));
        version.setSingleChoiceTotal(safeInt(submission.getSingleChoiceTotal()));
        
        version.setMultipleChoiceScore(safeInt(submission.getMultipleChoiceScore()));
        version.setMultipleChoiceMaxScore(safeInt(submission.getMultipleChoiceMaxScore()));
        version.setMultipleChoiceCorrect(safeInt(submission.getMultipleChoiceCorrect()));
        version.setMultipleChoiceTotal(safeInt(submission.getMultipleChoiceTotal()));
        
        version.setTrueFalseScore(safeInt(submission.getTrueFalseScore()));
        version.setTrueFalseMaxScore(safeInt(submission.getTrueFalseMaxScore()));
        version.setTrueFalseCorrect(safeInt(submission.getTrueFalseCorrect()));
        version.setTrueFalseTotal(safeInt(submission.getTrueFalseTotal()));
        
        version.setFillBlankScore(safeInt(submission.getFillBlankScore()));
        version.setFillBlankMaxScore(safeInt(submission.getFillBlankMaxScore()));
        version.setFillBlankCorrect(safeInt(submission.getFillBlankCorrect()));
        version.setFillBlankTotal(safeInt(submission.getFillBlankTotal()));
        
        version.setShortAnswerScore(safeInt(submission.getShortAnswerScore()));
        version.setShortAnswerMaxScore(safeInt(submission.getShortAnswerMaxScore()));
        version.setShortAnswerGraded(safeInt(submission.getShortAnswerGraded()));
        version.setShortAnswerTotal(safeInt(submission.getShortAnswerTotal()));
        
        version.setEssayScore(safeInt(submission.getEssayScore()));
        version.setEssayMaxScore(safeInt(submission.getEssayMaxScore()));
        version.setEssayGraded(safeInt(submission.getEssayGraded()));
        version.setEssayTotal(safeInt(submission.getEssayTotal()));
        
        version.setCodingScore(safeInt(submission.getCodingScore()));
        version.setCodingMaxScore(safeInt(submission.getCodingMaxScore()));
        version.setCodingGraded(safeInt(submission.getCodingGraded()));
        version.setCodingTotal(safeInt(submission.getCodingTotal()));
        
        SubmissionVersion savedVersion = submissionVersionRepository.save(version);
        
        if (submission.getAnswers() != null && !submission.getAnswers().isEmpty()) {
            List<SubmissionAnswerVersion> answerVersions = new ArrayList<>();
            for (SubmissionAnswer answer : submission.getAnswers()) {
                SubmissionAnswerVersion answerVersion = new SubmissionAnswerVersion();
                answerVersion.setSubmissionVersion(savedVersion);
                answerVersion.setQuestion(answer.getQuestion());
                answerVersion.setQuestionType(answer.getQuestion().getType());
                answerVersion.setQuestionOrderIndex(answer.getQuestion().getOrderIndex());
                answerVersion.setQuestionScore(answer.getQuestion().getScore());
                answerVersion.setQuestionCorrectAnswer(answer.getQuestion().getCorrectAnswer());
                answerVersion.setQuestionAutoGradable(answer.getQuestion().getAutoGradable());
                answerVersion.setStudentAnswer(answer.getStudentAnswer());
                answerVersion.setAutoScore(answer.getAutoScore());
                answerVersion.setManualScore(answer.getManualScore());
                
                Integer effectiveScore = 0;
                if (answer.getQuestion().getAutoGradable() && answer.getAutoScore() != null) {
                    effectiveScore = answer.getAutoScore();
                } else if (answer.getManualScore() != null) {
                    effectiveScore = answer.getManualScore();
                }
                answerVersion.setEffectiveScore(effectiveScore);
                
                answerVersion.setIsCorrect(answer.getIsCorrect());
                answerVersion.setTeacherFeedback(answer.getTeacherFeedback());
                answerVersion.setKnowledgePoints(answer.getQuestion().getKnowledgePoints());
                
                answerVersions.add(answerVersion);
            }
        }
        
        return savedVersion;
    }

    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }

    @Transactional
    public void updateSubmissionScores(Submission submission) {
        log.info("更新提交 {} 的分数计算", submission.getId());
        
        List<Question> questions = questionRepository.findByAssignmentIdOrderByOrderIndexAsc(
                submission.getAssignment().getId());
        List<SubmissionAnswer> answers = submissionAnswerRepository.findBySubmissionId(submission.getId());
        
        ScoreCalculationService.SubmissionScoreResult result = 
                scoreCalculationService.recalculateAndFixScores(questions, answers);
        
        scoreCalculationService.applyScoresToSubmission(submission, result);
        
        log.info("分数计算结果：autoScore={}, manualScore={}, totalScore={}, " +
                "objectiveScore={}/{}, subjectiveScore={}/{}, " +
                "objectiveAccuracy={}%, subjectiveScoreRate={}%",
                result.getAutoScore(), result.getManualScore(), result.getTotalScore(),
                result.getObjectiveScore(), result.getObjectiveMaxScore(),
                result.getSubjectiveScore(), result.getSubjectiveMaxScore(),
                result.getObjectiveAccuracy(), result.getSubjectiveScoreRate());
        
        if (!result.isValid()) {
            log.warn("分数计算存在校验问题：{}", result.getValidationErrors());
        }
    }

    public Submission getSubmissionById(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
    }

    public List<SubmissionVersion> getSubmissionVersions(Long submissionId) {
        return submissionVersionRepository.findBySubmissionIdOrderByVersionNumberDesc(submissionId);
    }

    public SubmissionVersion getSubmissionVersion(Long submissionId, Integer versionNumber) {
        return submissionVersionRepository.findBySubmissionIdAndVersionNumber(submissionId, versionNumber)
                .orElseThrow(() -> new RuntimeException("版本不存在"));
    }

    public SubmissionVersion getLatestVersion(Long submissionId) {
        return submissionVersionRepository.findBySubmissionIdAndIsLatestTrue(submissionId)
                .orElseGet(() -> submissionVersionRepository.findTopBySubmissionIdOrderByVersionNumberDesc(submissionId)
                        .orElseThrow(() -> new RuntimeException("没有找到版本记录")));
    }

    public List<Submission> getSubmissionsByAssignmentId(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    public List<Submission> getSubmissionsByStudentId(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }

    public List<Submission> getGradedSubmissionsByStudentId(Long studentId) {
        return submissionRepository.findByStudentIdAndStatusIn(
                studentId,
                List.of(AssignmentStatus.GRADED, AssignmentStatus.RETURNED, AssignmentStatus.ARCHIVED)
        );
    }

    public Submission getSubmissionByAssignmentAndStudent(Long assignmentId, Long studentId) {
        return submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
    }

    @Transactional
    public Submission returnSubmission(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
        
        if (submission.getStatus() != AssignmentStatus.GRADED) {
            throw new RuntimeException("只有已批改状态的作业可以返回");
        }
        
        submission.setStatus(AssignmentStatus.RETURNED);
        submission.setReturnedAt(LocalDateTime.now());
        
        createVersionFromSubmission(submission, true);
        
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
