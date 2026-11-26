package com.medicare.health.repository;

import com.medicare.health.entity.HealthGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthGoalRepository extends JpaRepository<HealthGoal, Long> {
    List<HealthGoal> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    Optional<HealthGoal> findByIdAndPatientId(Long id, Long patientId);
    List<HealthGoal> findByPatientIdAndStatus(Long patientId, String status);
}
