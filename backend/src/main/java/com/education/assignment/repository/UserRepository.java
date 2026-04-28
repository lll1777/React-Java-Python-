package com.education.assignment.repository;

import com.education.assignment.entity.User;
import com.education.assignment.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(UserRole role);
    List<User> findByRoleAndClassesId(UserRole role, Long classId);
}
