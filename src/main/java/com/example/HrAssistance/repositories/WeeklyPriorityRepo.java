//package com.example.WingPulse.repositories;
//
//import com.example.WingPulse.enums.PriorityStatus;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface WeeklyPriorityRepo extends JpaRepository<WeeklyPriority, Long> {
//    List<WeeklyPriority> findByUserIdAndWeekNumberAndYear(Long userId, Integer weekNumber, Integer year);
//    List<WeeklyPriority> findByUserIdAndStatus(Long userId, PriorityStatus status);
////    List<WeeklyPriority> findByTeamIdAndWeekNumberAndYear(Long teamId, Integer weekNumber, Integer year);
////
////    @Query("SELECT wp FROM WeeklyPriority wp WHERE wp.user.team.id = :teamId AND wp.weekNumber = :weekNumber AND wp.year = :year")
////    List<WeeklyPriority> findTeamPriorities(@Param("teamId") Long teamId,
////                                            @Param("weekNumber") Integer weekNumber,
////                                            @Param("year") Integer year);
////
////    @Query("SELECT COUNT(wp) FROM WeeklyPriority wp WHERE wp.user.id = :userId AND wp.status = :status AND wp.weekNumber = :weekNumber AND wp.year = :year")
////    Long countByUserAndStatusAndWeek(@Param("userId") Long userId,
////                                     @Param("status") PriorityStatus status,
////                                     @Param("weekNumber") Integer weekNumber,
////                                     @Param("year") Integer year);
//}
