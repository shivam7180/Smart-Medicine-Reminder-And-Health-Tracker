package com.medicare.analytics.repository;

import com.medicare.analytics.entity.AdherenceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdherenceSnapshotRepository extends JpaRepository<AdherenceSnapshot, Long> {
    Optional<AdherenceSnapshot> findByPatientIdAndSnapshotDate(Long patientId, LocalDate snapshotDate);
    List<AdherenceSnapshot> findByPatientIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            Long patientId, LocalDate startDate, LocalDate endDate);
}
