package com.example.HrAssistance.repositories;

import com.example.HrAssistance.model.Email;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface EmailRepo {
    List<Email> findByCandidateIdOrderBySentAtDesc(Long candidateId);
}
