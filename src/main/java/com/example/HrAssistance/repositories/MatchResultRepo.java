package com.example.HrAssistance.repositories;

import com.example.HrAssistance.model.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchResultRepo extends JpaRepository<MatchResult, Long> {

    // Get all results for a specific JD ordered by score
    List<MatchResult> findByJobDescriptionIdOrderByMatchScoreDesc(Long jdId);

    // Get all results for a specific candidate
    List<MatchResult> findByCandidateId(Long candidateId);
}