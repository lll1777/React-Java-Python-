package com.education.assignment.repository;

import com.education.assignment.entity.LearningReport;
import com.education.assignment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LearningReportRepository extends JpaRepository<LearningReport, Long> {
    List<LearningReport> findByStudent(User student);
    List<LearningReport> findByStudentId(Long studentId);
    List<LearningReport> findByStudentIdOrderByGeneratedAtDesc(Long studentId);
    
    @Query("SELECT lr FROM LearningReport lr WHERE lr.student.id = :studentId AND lr.reportType = :reportType ORDER BY lr.generatedAt DESC")
    List<LearningReport> findByStudentIdAndReportType(@Param("studentId") Long studentId, @Param("reportType") LearningReport.ReportType reportType);
    
    @Query("SELECT lr FROM LearningReport lr WHERE lr.student.id = :studentId AND lr.generatedAt BETWEEN :start AND :end ORDER BY lr.generatedAt DESC")
    List<LearningReport> findByStudentIdAndDateRange(@Param("studentId") Long studentId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT lr FROM LearningReport lr WHERE lr.student.id = :studentId ORDER BY lr.generatedAt DESC LIMIT 1")
    LearningReport findLatestByStudentId(@Param("studentId") Long studentId);
}
