package com.education.assignment.repository;

import com.education.assignment.entity.Assignment;
import com.education.assignment.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByAssignmentOrderByOrderIndexAsc(Assignment assignment);
    List<Question> findByAssignmentIdOrderByOrderIndexAsc(Long assignmentId);
    Optional<Question> findByAssignmentIdAndOrderIndex(Long assignmentId, Integer orderIndex);
    
    @Query("SELECT SUM(q.score) FROM Question q WHERE q.assignment.id = :assignmentId")
    Integer getTotalScoreByAssignmentId(@Param("assignmentId") Long assignmentId);
    
    @Query("SELECT q FROM Question q WHERE q.autoGradable = true AND q.assignment.id = :assignmentId")
    List<Question> findAutoGradableQuestionsByAssignmentId(@Param("assignmentId") Long assignmentId);
    
    @Query("SELECT COUNT(q) FROM Question q WHERE q.assignment.id = :assignmentId")
    Long countByAssignmentId(@Param("assignmentId") Long assignmentId);
}
