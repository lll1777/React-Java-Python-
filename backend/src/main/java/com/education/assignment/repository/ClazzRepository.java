package com.education.assignment.repository;

import com.education.assignment.entity.Clazz;
import com.education.assignment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClazzRepository extends JpaRepository<Clazz, Long> {
    List<Clazz> findByTeacher(User teacher);
    
    @Query("SELECT c FROM Clazz c JOIN c.students s WHERE s.id = :studentId")
    List<Clazz> findByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT COUNT(s) FROM Clazz c JOIN c.students s WHERE c.id = :classId")
    Long countStudentsByClassId(@Param("classId") Long classId);
}
