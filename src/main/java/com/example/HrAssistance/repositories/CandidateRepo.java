package com.example.HrAssistance.repositories;

import com.example.HrAssistance.enums.CandidateStatus;
import com.example.HrAssistance.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CandidateRepo extends JpaRepository<Candidate, Long> {
    List<Candidate> findByDomain(String domain);
    List<Candidate> findByStatus(CandidateStatus status);
    List<Candidate> findByDomainAndExpYearsGreaterThanEqual(String domain, Integer expYears);

    // For chat — get all when domain is null
    @Query("SELECT c FROM Candidate c WHERE " +
            "(:domain IS NULL OR c.domain = :domain) AND " +
            "(:minExp IS NULL OR c.expYears >= :minExp)")
    List<Candidate> findByFilters(
            @Param("domain") String domain,
            @Param("minExp") Integer minExp
    );
}