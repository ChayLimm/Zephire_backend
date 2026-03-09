package com.example.HrAssistance.repositories;

import com.example.HrAssistance.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepo extends JpaRepository<ChatMessage, Long> {

    // Get chat history for a user
    List<ChatMessage> findByUserIdOrderByCreatedAtAsc(Long userId);

    // Get chat history for a specific JD
    List<ChatMessage> findByJobDescriptionIdOrderByCreatedAtAsc(Long jdId);

    // Delete all chat messages for a user
    void deleteByUserId(Long userId);
    List<ChatMessage> findByCandidateIdOrderByCreatedAtAsc(Long candidateId);
    void deleteByUserIdAndJobDescriptionIsNullAndCandidateIsNull(Long userId);
    List<ChatMessage> findByUserIdAndJobDescriptionIsNullAndCandidateIsNullOrderByCreatedAtAsc(Long userId);
}