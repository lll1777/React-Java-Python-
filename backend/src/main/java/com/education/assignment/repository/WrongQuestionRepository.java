package com.education.assignment.repository;

import com.education.assignment.entity.User;
import com.education.assignment.entity.WrongQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WrongQuestionRepository extends JpaRepository<WrongQuestion, Long> {
    List<WrongQuestion> findByStudent(User student);
    List<WrongQuestion> findByStudentId(Long studentId);
    List<WrongQuestion> findByStudentIdAndIsResolved(Long studentId, Boolean isResolved);
    Optional<WrongQuestion> findByStudentIdAndQuestionId(Long studentId, Long questionId);
    
    @Query("SELECT wq FROM WrongQuestion wq WHERE wq.student.id = :studentId AND wq.question.assignment.clazz.id = :classId")
    List<WrongQuestion> findByStudentIdAndClassId(@Param("studentId") Long studentId, @Param("classId") Long classId);
    
    @Query("SELECT wq FROM WrongQuestion wq WHERE wq.student.id = :studentId AND wq.isResolved = :isResolved ORDER BY wq.lastWrongAt DESC")
    List<WrongQuestion> findByStudentIdAndIsResolvedOrderByLastWrongAtDesc(@Param("studentId") Long studentId, @Param("isResolved") Boolean isResolved);
    
    @Query("SELECT COUNT(wq) FROM WrongQuestion wq WHERE wq.student.id = :studentId AND wq.isResolved = :isResolved")
    Long countByStudentIdAndIsResolved(@Param("studentId") Long studentId, @Param("isResolved") Boolean isResolved);
    
    @Query("SELECT wq.question.knowledgePoints, COUNT(wq) FROM WrongQuestion wq WHERE wq.student.id = :studentId AND wq.isResolved = false GROUP BY wq.question.knowledgePoints")
    List<Object[]> countWrongQuestionsByKnowledgePoint(@Param("studentId") Long studentId);
}
