package com.education.assignment.repository;

import com.education.assignment.entity.Submission;
import com.education.assignment.entity.SubmissionAnswer;
import com.education.assignment.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionAnswerRepository extends JpaRepository<SubmissionAnswer, Long> {
    List<SubmissionAnswer> findBySubmission(Submission submission);
    List<SubmissionAnswer> findBySubmissionId(Long submissionId);
    Optional<SubmissionAnswer> findBySubmissionAndQuestion(Submission submission, Question question);
    Optional<SubmissionAnswer> findBySubmissionIdAndQuestionId(Long submissionId, Long questionId);
    
    @Query("SELECT sa FROM SubmissionAnswer sa WHERE sa.submission.student.id = :studentId AND sa.question.id = :questionId ORDER BY sa.submission.submittedAt DESC")
    List<SubmissionAnswer> findByStudentIdAndQuestionId(@Param("studentId") Long studentId, @Param("questionId") Long questionId);
    
    @Query("SELECT COUNT(sa) FROM SubmissionAnswer sa WHERE sa.question.id = :questionId AND sa.isCorrect = true")
    Long countCorrectByQuestionId(@Param("questionId") Long questionId);
    
    @Query("SELECT COUNT(sa) FROM SubmissionAnswer sa WHERE sa.question.id = :questionId")
    Long countByQuestionId(@Param("questionId") Long questionId);
    
    @Query("SELECT SUM(sa.autoScore) FROM SubmissionAnswer sa WHERE sa.submission.id = :submissionId")
    Integer getTotalAutoScoreBySubmissionId(@Param("submissionId") Long submissionId);
    
    @Query("SELECT SUM(sa.manualScore) FROM SubmissionAnswer sa WHERE sa.submission.id = :submissionId")
    Integer getTotalManualScoreBySubmissionId(@Param("submissionId") Long submissionId);
}
