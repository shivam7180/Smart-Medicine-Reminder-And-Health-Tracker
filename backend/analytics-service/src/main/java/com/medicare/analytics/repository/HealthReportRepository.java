package com.medicare.analytics.repository;

import com.medicare.analytics.entity.HealthReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthReportRepository extends JpaRepository<HealthReport, Long> {
    List<HealthReport> findByPatientIdOrderByGeneratedAtDesc(Long patientId);
}
