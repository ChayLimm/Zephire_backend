//package com.example.WingPulse.repositories;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.awt.print.Pageable;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Repository
//public interface RecognitionRepo extends JpaRepository<Recognition, Long> {
//    List<Recognition> findByReceiverId(Long receiverId);
//    List<Recognition> findByGiverId(Long giverId);
//    List<Recognition> findByReceiverIdAndTimestampBetween(Long receiverId, LocalDateTime start, LocalDateTime end);
//
//    @Query("SELECT r.value, COUNT(r) FROM Recognition r WHERE r.receiver.team.id = :teamId AND r.timestamp >= :startDate GROUP BY r.value")
//    List<Object[]> countRecognitionsByValueForTeam(@Param("teamId") Long teamId,
//                                                   @Param("startDate") LocalDateTime startDate);
//
//    @Query("SELECT r.receiver.id, COUNT(r) as recognitionCount FROM Recognition r WHERE r.timestamp >= :startDate GROUP BY r.receiver.id ORDER BY recognitionCount DESC")
//    List<Object[]> findTopRecognizedEmployees(@Param("startDate") LocalDateTime startDate, Pageable pageable);
//}