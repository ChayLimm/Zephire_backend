package com.example.HrAssistance.repositories;

import com.example.HrAssistance.model.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobDescriptionRepo extends JpaRepository<JobDescription, Long> {
    List<JobDescription> findByCreatedById(Long userId);
    List<JobDescription> findByField(String field);
    List<JobDescription> findAllByOrderByCreatedAtDesc();

}