package com.education.assignment.service;

import com.education.assignment.dto.SubmissionCreateDTO;
import com.education.assignment.entity.*;
import com.education.assignment.enums.AssignmentStatus;
import com.education.assignment.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final SubmissionVersionRepository submissionVersionRepository;
    private final SubmissionAnswerRepository submissionAnswerRepository;
    private final AssignmentRepository assignmentRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Submission submitAssignment(SubmissionCreateDTO dto, Long studentId, String versionNote) {
        Assignment assignment = assignmentRepository.findById(dto.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("作业不存在"));
        
        if (assignment.getStatus() != AssignmentStatus.OPEN && 
            assignment.getStatus() != AssignmentStatus.PUBLISHED) {
            throw new RuntimeException("作业未开放提交");
        }
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        boolean isLate = LocalDateTime.now().isAfter(assignment.getDeadline());
        
        Optional<Submission> existingSubmission = submissionRepository.findByAssignmentAndStudent(assignment, student);
        
        Submission submission;
        List<SubmissionAnswer> newAnswers = new ArrayList<>();
        int newVersionNumber;
        
        if (existingSubmission.isPresent()) {
            submission = existingSubmission.get();
            
            Integer maxVersion = submissionVersionRepository.findMaxVersionNumberBySubmissionId(submission.getId());
            newVersionNumber = (maxVersion != null ? maxVersion : submission.getVersionNumber()) + 1;
            
            SubmissionVersion previousVersion = createVersionFromSubmission(submission);
            submissionVersionRepository.save(previousVersion);
            
            submissionAnswerRepository.deleteAll(submission.getAnswers());
            submission.getAnswers().clear();
            
        } else {
            submission = new Submission();
            submission.setAssignment(assignment);
            submission.setStudent(student);
            newVersionNumber = 1;
        }
        
        submission.setStatus(AssignmentStatus.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setIsLate(isLate);
        submission.setVersionNumber(newVersionNumber);
        submission.setVersionNote(versionNote);
        
        submission.setAutoScore(null);
        submission.setManualScore(null);
        submission.setTotalScore(null);
        submission.setObjectiveScore(null);
        submission.setSubjectiveScore(null);
        submission.setObjectiveAccuracy(null);
        submission.setSubjectiveScoreRate(null);
        submission.setAutoGradedAt(null);
        submission.setManuallyGradedAt(null);
        submission.setTeacherComments(null);
        
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
                
                SubmissionAnswer savedAnswer = submissionAnswerRepository.save(answer);
                newAnswers.add(savedAnswer);
            }
            savedSubmission.setAnswers(newAnswers);
        }
        
        createLatestVersion(savedSubmission, newAnswers);
        
        return savedSubmission;
    }

    private SubmissionVersion createVersionFromSubmission(Submission submission) {
        SubmissionVersion version = new SubmissionVersion();
        version.setSubmission(submission);
        version.setVersionNumber(submission.getVersionNumber());
        version.setSubmittedAt(submission.getSubmittedAt());
        version.setAutoGradedAt(submission.getAutoGradedAt());
        version.setManuallyGradedAt(submission.getManuallyGradedAt());
        version.setAutoScore(submission.getAutoScore());
        version.setManualScore(submission.getManualScore());
        version.setTotalScore(submission.getTotalScore());
        version.setObjectiveScore(submission.getObjectiveScore());
        version.setObjectiveMaxScore(submission.getObjectiveMaxScore());
        version.setSubjectiveScore(submission.getSubjectiveScore());
        version.setSubjectiveMaxScore(submission.getSubjectiveMaxScore());
        version.setObjectiveAccuracy(submission.getObjectiveAccuracy());
        version.setSubjectiveScoreRate(submission.getSubjectiveScoreRate());
        version.setTeacherComments(submission.getTeacherComments());
        version.setVersionNote(submission.getVersionNote());
        version.setIsLatest(false);
        
        for (SubmissionAnswer answer : submission.getAnswers()) {
            SubmissionAnswerVersion answerVersion = new SubmissionAnswerVersion();
            answerVersion.setSubmissionVersion(version);
            answerVersion.setQuestion(answer.getQuestion());
            answerVersion.setStudentAnswer(answer.getStudentAnswer());
            answerVersion.setAutoScore(answer.getAutoScore());
            answerVersion.setManualScore(answer.getManualScore());
            answerVersion.setTeacherFeedback(answer.getTeacherFeedback());
            answerVersion.setIsCorrect(answer.getIsCorrect());
            answerVersion.setQuestionType(answer.getQuestion().getType());
            answerVersion.setQuestionScore(answer.getQuestion().getScore());
            answerVersion.setAutoGradable(answer.getQuestion().getAutoGradable());
            
            version.getAnswers().add(answerVersion);
        }
        
        return version;
    }

    private void createLatestVersion(Submission submission, List<SubmissionAnswer> answers) {
        SubmissionVersion latestVersion = new SubmissionVersion();
        latestVersion.setSubmission(submission);
        latestVersion.setVersionNumber(submission.getVersionNumber());
        latestVersion.setSubmittedAt(submission.getSubmittedAt());
        latestVersion.setIsLatest(true);
        
        for (SubmissionAnswer answer : answers) {
            SubmissionAnswerVersion answerVersion = new SubmissionAnswerVersion();
            answerVersion.setSubmissionVersion(latestVersion);
            answerVersion.setQuestion(answer.getQuestion());
            answerVersion.setStudentAnswer(answer.getStudentAnswer());
            answerVersion.setQuestionType(answer.getQuestion().getType());
            answerVersion.setQuestionScore(answer.getQuestion().getScore());
            answerVersion.setAutoGradable(answer.getQuestion().getAutoGradable());
            
            latestVersion.getAnswers().add(answerVersion);
        }
        
        submissionVersionRepository.save(latestVersion);
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

    public List<SubmissionVersion> getSubmissionVersions(Long submissionId) {
        return submissionVersionRepository.findBySubmissionIdOrderByVersionNumberDesc(submissionId);
    }

    public SubmissionVersion getSubmissionVersion(Long submissionId, Integer versionNumber) {
        return submissionVersionRepository.findBySubmissionIdAndVersionNumber(submissionId, versionNumber)
                .orElseThrow(() -> new RuntimeException("版本不存在"));
    }

    public SubmissionVersion getLatestVersion(Long submissionId) {
        return submissionVersionRepository.findBySubmissionIdAndIsLatestTrue(submissionId)
                .orElseThrow(() -> new RuntimeException("最新版本不存在"));
    }

    public Integer getVersionCount(Long submissionId) {
        Long count = submissionVersionRepository.countBySubmissionId(submissionId);
        return count != null ? count.intValue() : 0;
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
        
        Submission savedSubmission = submissionRepository.save(submission);
        
        Optional<SubmissionVersion> latestVersion = submissionVersionRepository.findBySubmissionIdAndIsLatestTrue(submissionId);
        if (latestVersion.isPresent()) {
            SubmissionVersion version = latestVersion.get();
            version.setStatus(AssignmentStatus.RETURNED);
            submissionVersionRepository.save(version);
        }
        
        return savedSubmission;
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

    @Transactional
    public void updateSubmissionScores(Submission submission) {
        List<SubmissionAnswer> answers = submissionAnswerRepository.findBySubmissionId(submission.getId());
        
        int objectiveScore = 0;
        int subjectiveScore = 0;
        int objectiveMaxScore = 0;
        int subjectiveMaxScore = 0;
        int correctObjective = 0;
        int totalObjective = 0;
        int gradedSubjective = 0;
        int totalSubjective = 0;
        
        for (SubmissionAnswer answer : answers) {
            Question question = answer.getQuestion();
            
            if (question.getAutoGradable()) {
                totalObjective++;
                objectiveMaxScore += question.getScore();
                
                if (answer.getAutoScore() != null) {
                    objectiveScore += answer.getAutoScore();
                    if (answer.getIsCorrect() != null && answer.getIsCorrect()) {
                        correctObjective++;
                    }
                }
            } else {
                totalSubjective++;
                subjectiveMaxScore += question.getScore();
                
                if (answer.getManualScore() != null) {
                    subjectiveScore += answer.getManualScore();
                    gradedSubjective++;
                }
            }
        }
        
        submission.setObjectiveScore(objectiveScore);
        submission.setObjectiveMaxScore(objectiveMaxScore);
        submission.setSubjectiveScore(subjectiveScore);
        submission.setSubjectiveMaxScore(subjectiveMaxScore);
        
        submission.setTotalObjectiveQuestions(totalObjective);
        submission.setCorrectObjectiveQuestions(correctObjective);
        submission.setWrongObjectiveQuestions(totalObjective - correctObjective);
        submission.setTotalSubjectiveQuestions(totalSubjective);
        submission.setGradedSubjectiveQuestions(gradedSubjective);
        
        if (objectiveMaxScore > 0) {
            submission.setObjectiveAccuracy((double) objectiveScore / objectiveMaxScore * 100);
        }
        
        if (subjectiveMaxScore > 0 && gradedSubjective == totalSubjective) {
            submission.setSubjectiveScoreRate((double) subjectiveScore / subjectiveMaxScore * 100);
        }
        
        submission.setAutoScore(objectiveScore);
        submission.setManualScore(subjectiveScore);
        submission.setTotalScore(objectiveScore + subjectiveScore);
        
        submissionRepository.save(submission);
        
        Optional<SubmissionVersion> latestVersionOpt = submissionVersionRepository.findBySubmissionIdAndIsLatestTrue(submission.getId());
        if (latestVersionOpt.isPresent()) {
            SubmissionVersion latestVersion = latestVersionOpt.get();
            latestVersion.setAutoScore(objectiveScore);
            latestVersion.setManualScore(subjectiveScore);
            latestVersion.setTotalScore(objectiveScore + subjectiveScore);
            latestVersion.setObjectiveScore(objectiveScore);
            latestVersion.setObjectiveMaxScore(objectiveMaxScore);
            latestVersion.setSubjectiveScore(subjectiveScore);
            latestVersion.setSubjectiveMaxScore(subjectiveMaxScore);
            latestVersion.setObjectiveAccuracy(submission.getObjectiveAccuracy());
            latestVersion.setSubjectiveScoreRate(submission.getSubjectiveScoreRate());
            submissionVersionRepository.save(latestVersion);
        }
    }
}
