package com.medicare.health.service;

import com.medicare.health.dto.HealthGoalRequest;
import com.medicare.health.dto.HealthGoalResponse;

import java.math.BigDecimal;
import java.util.List;

public interface HealthGoalService {
    HealthGoalResponse createGoal(Long patientId, HealthGoalRequest request);
    HealthGoalResponse updateGoalProgress(Long patientId, Long goalId, BigDecimal currentValue);
    HealthGoalResponse updateGoal(Long patientId, Long goalId, HealthGoalRequest request);
    List<HealthGoalResponse> getGoals(Long patientId);
    void deleteGoal(Long patientId, Long goalId);
}
