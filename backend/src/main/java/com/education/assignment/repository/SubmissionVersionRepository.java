package com.education.assignment.repository;

import com.education.assignment.entity.Submission;
import com.education.assignment.entity.SubmissionVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionVersionRepository extends JpaRepository<SubmissionVersion, Long> {
    
    List<SubmissionVersion> findBySubmissionOrderByVersionNumberDesc(Submission submission);
    
    List<SubmissionVersion> findBySubmissionIdOrderByVersionNumberDesc(Long submissionId);
    
    Optional<SubmissionVersion> findBySubmissionIdAndVersionNumber(Long submissionId, Integer versionNumber);
    
    Optional<SubmissionVersion> findBySubmissionIdAndIsLatestTrue(Long submissionId);
    
    Optional<SubmissionVersion> findTopBySubmissionIdOrderByVersionNumberDesc(Long submissionId);
    
    @Query("SELECT MAX(v.versionNumber) FROM SubmissionVersion v WHERE v.submission.id = :submissionId")
    Integer findMaxVersionNumberBySubmissionId(@Param("submissionId") Long submissionId);
    
    @Query("SELECT COUNT(v) FROM SubmissionVersion v WHERE v.submission.id = :submissionId")
    Long countBySubmissionId(@Param("submissionId") Long submissionId);
    
    @Query("SELECT v FROM SubmissionVersion v WHERE v.submission.assignment.id = :assignmentId AND v.submission.student.id = :studentId ORDER BY v.versionNumber DESC")
    List<SubmissionVersion> findByAssignmentIdAndStudentId(@Param("assignmentId") Long assignmentId, @Param("studentId") Long studentId);
    
    @Query("SELECT v FROM SubmissionVersion v WHERE v.submission.assignment.id = :assignmentId AND v.isLatest = true ORDER BY v.submittedAt DESC")
    List<SubmissionVersion> findLatestVersionsByAssignmentId(@Param("assignmentId") Long assignmentId);
}
