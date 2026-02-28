package com.example.HrAssistance.repositories;

import com.example.HrAssistance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User>  findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
//    List<User> findByDepartment(Department department);
//    List<User> findByTeamId(Long teamId);
//    List<User> findByRole(Role role);
    long count();
}