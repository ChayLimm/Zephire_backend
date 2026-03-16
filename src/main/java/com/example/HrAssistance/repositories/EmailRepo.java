package com.example.HrAssistance.repositories;

import com.example.HrAssistance.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Collection;
import java.util.List;

@Repository
public interface EmailRepo extends JpaRepository<Email, Long> {
    List<Email> findByCandidateIdOrderBySentAtDesc(Long candidateId);

    List<Email> findAllByOrderBySentAtDesc();
}