package com.education.assignment.repository;

import com.education.assignment.entity.Assignment;
import com.education.assignment.entity.Submission;
import com.education.assignment.entity.User;
import com.education.assignment.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByAssignmentAndStudent(Assignment assignment, User student);
    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    List<Submission> findByAssignmentId(Long assignmentId);
    List<Submission> findByStudentId(Long studentId);
    List<Submission> findByAssignmentIdAndStatus(Long assignmentId, AssignmentStatus status);
    List<Submission> findByStudentIdAndStatus(Long studentId, AssignmentStatus status);
    
    @Query("SELECT s FROM Submission s WHERE s.assignment.id = :assignmentId AND s.status IN :statuses")
    List<Submission> findByAssignmentIdAndStatusIn(@Param("assignmentId") Long assignmentId, @Param("statuses") List<AssignmentStatus> statuses);
    
    @Query("SELECT s FROM Submission s WHERE s.student.id = :studentId AND s.assignment.clazz.id = :classId")
    List<Submission> findByStudentIdAndClassId(@Param("studentId") Long studentId, @Param("classId") Long classId);
    
    @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignment.id = :assignmentId")
    Long countByAssignmentId(@Param("assignmentId") Long assignmentId);
    
    @Query("SELECT AVG(s.totalScore) FROM Submission s WHERE s.assignment.id = :assignmentId AND s.totalScore IS NOT NULL")
    Double getAverageScoreByAssignmentId(@Param("assignmentId") Long assignmentId);
    
    @Query("SELECT MAX(s.totalScore) FROM Submission s WHERE s.assignment.id = :assignmentId AND s.totalScore IS NOT NULL")
    Integer getHighestScoreByAssignmentId(@Param("assignmentId") Long assignmentId);
    
    @Query("SELECT MIN(s.totalScore) FROM Submission s WHERE s.assignment.id = :assignmentId AND s.totalScore IS NOT NULL")
    Integer getLowestScoreByAssignmentId(@Param("assignmentId") Long assignmentId);
    
    @Query("SELECT s FROM Submission s WHERE s.student.id = :studentId AND s.status = :status ORDER BY s.submittedAt DESC")
    List<Submission> findRecentSubmissionsByStudent(@Param("studentId") Long studentId, @Param("status") AssignmentStatus status);
}
