package com.medicare.health.repository;

import com.medicare.health.entity.HealthRecord;
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
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {

    List<HealthRecord> findByPatientIdAndRecordTypeOrderByReadingTimeDesc(Long patientId, String recordType);

    List<HealthRecord> findByPatientIdAndRecordTypeAndReadingTimeBetweenOrderByReadingTimeAsc(
            Long patientId, String recordType, LocalDateTime start, LocalDateTime end);

    Page<HealthRecord> findByPatientIdOrderByReadingTimeDesc(Long patientId, Pageable pageable);

    Page<HealthRecord> findByPatientIdAndRecordTypeOrderByReadingTimeDesc(Long patientId, String recordType, Pageable pageable);

    Optional<HealthRecord> findByIdAndPatientId(Long id, Long patientId);

    // Get the very latest record for each record type
    @Query("SELECT r FROM HealthRecord r WHERE r.id IN (" +
           "  SELECT MAX(sub.id) FROM HealthRecord sub " +
           "  WHERE sub.patientId = :patientId " +
           "  GROUP BY sub.recordType)")
    List<HealthRecord> findLatestRecordsForEachType(@Param("patientId") Long patientId);

    Optional<HealthRecord> findFirstByPatientIdAndRecordTypeOrderByReadingTimeDesc(Long patientId, String recordType);
}
