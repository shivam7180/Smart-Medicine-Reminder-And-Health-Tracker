package com.medicare.reminder.repository;

import com.medicare.common.enums.DoseStatus;
import com.medicare.reminder.entity.DoseLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoseLogRepository extends JpaRepository<DoseLog, Long> {

    List<DoseLog> findByPatientIdAndScheduledTimeBetweenOrderByScheduledTimeAsc(
            Long patientId, LocalDateTime startTime, LocalDateTime endTime);

    Page<DoseLog> findByPatientIdOrderByScheduledTimeDesc(Long patientId, Pageable pageable);

    List<DoseLog> findByPatientIdAndStatusOrderByScheduledTimeDesc(Long patientId, DoseStatus status);

    boolean existsByScheduleIdAndScheduledTime(Long scheduleId, LocalDateTime scheduledTime);

    Optional<DoseLog> findByIdAndPatientId(Long id, Long patientId);

    // Query for overdue scheduled doses needing transition to MISSED
    @Query("SELECT d FROM DoseLog d WHERE d.status = :status AND d.scheduledTime <= :cutoffTime")
    List<DoseLog> findOverdueScheduledDoses(
            @Param("status") DoseStatus status,
            @Param("cutoffTime") LocalDateTime cutoffTime);

    // Query for today's summary metrics
    @Query("SELECT COUNT(d) FROM DoseLog d WHERE d.patientId = :patientId " +
           "AND d.scheduledTime BETWEEN :startOfDay AND :endOfDay")
    long countTodayScheduled(
            @Param("patientId") Long patientId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(d) FROM DoseLog d WHERE d.patientId = :patientId " +
           "AND d.status = :status " +
           "AND d.scheduledTime BETWEEN :startOfDay AND :endOfDay")
    long countTodayByStatus(
            @Param("patientId") Long patientId,
            @Param("status") DoseStatus status,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    // Overall adherence calculation
    @Query("SELECT COUNT(d) FROM DoseLog d WHERE d.patientId = :patientId " +
           "AND d.scheduledTime BETWEEN :startTime AND :endTime")
    long countTotalInRange(@Param("patientId") Long patientId,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(d) FROM DoseLog d WHERE d.patientId = :patientId " +
           "AND d.status = 'TAKEN' " +
           "AND d.scheduledTime BETWEEN :startTime AND :endTime")
    long countTakenInRange(@Param("patientId") Long patientId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);
}
