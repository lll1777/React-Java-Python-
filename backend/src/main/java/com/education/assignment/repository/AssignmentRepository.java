package com.education.assignment.repository;

import com.education.assignment.entity.Assignment;
import com.education.assignment.entity.Clazz;
import com.education.assignment.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByClazz(Clazz clazz);
    List<Assignment> findByClazzId(Long clazzId);
    List<Assignment> findByCreatorId(Long creatorId);
    List<Assignment> findByStatus(AssignmentStatus status);
    List<Assignment> findByClazzIdAndStatus(Long clazzId, AssignmentStatus status);
    
    @Query("SELECT a FROM Assignment a WHERE a.clazz.id IN :classIds")
    List<Assignment> findByClassIds(@Param("classIds") List<Long> classIds);
    
    @Query("SELECT a FROM Assignment a JOIN a.clazz c JOIN c.students s WHERE s.id = :studentId")
    List<Assignment> findByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT a FROM Assignment a WHERE a.status = :status AND a.deadline < CURRENT_TIMESTAMP")
    List<Assignment> findOverdueAssignments(@Param("status") AssignmentStatus status);
    
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.clazz.id = :classId")
    Long countByClassId(@Param("classId") Long classId);
    
    @Query("SELECT AVG(a.totalScore) FROM Assignment a WHERE a.clazz.id = :classId")
    Double getAverageTotalScoreByClassId(@Param("classId") Long classId);
}
