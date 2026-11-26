package com.medicare.health.service.impl;

import com.medicare.common.exception.ResourceNotFoundException;
import com.medicare.health.dto.HealthGoalRequest;
import com.medicare.health.dto.HealthGoalResponse;
import com.medicare.health.entity.HealthGoal;
import com.medicare.health.repository.HealthGoalRepository;
import com.medicare.health.service.HealthGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthGoalServiceImpl implements HealthGoalService {

    private final HealthGoalRepository goalRepository;

    @Override
    @Transactional
    public HealthGoalResponse createGoal(Long patientId, HealthGoalRequest request) {
        HealthGoal goal = HealthGoal.builder()
                .patientId(patientId)
                .goalType(request.getGoalType().toUpperCase().trim())
                .targetValue(request.getTargetValue())
                .currentValue(request.getCurrentValue() != null ? request.getCurrentValue() : BigDecimal.ZERO)
                .unit(request.getUnit())
                .startDate(request.getStartDate())
                .targetDate(request.getTargetDate())
                .status("IN_PROGRESS")
                .build();

        return mapToResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public HealthGoalResponse updateGoalProgress(Long patientId, Long goalId, BigDecimal currentValue) {
        HealthGoal goal = goalRepository.findByIdAndPatientId(goalId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthGoal", "id", goalId));

        goal.setCurrentValue(currentValue);

        // Check achievement
        if (currentValue.compareTo(goal.getTargetValue()) >= 0) {
            goal.setStatus("ACHIEVED");
        } else {
            goal.setStatus("IN_PROGRESS");
        }

        return mapToResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public HealthGoalResponse updateGoal(Long patientId, Long goalId, HealthGoalRequest request) {
        HealthGoal goal = goalRepository.findByIdAndPatientId(goalId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthGoal", "id", goalId));

        goal.setGoalType(request.getGoalType().toUpperCase().trim());
        goal.setTargetValue(request.getTargetValue());
        if (request.getCurrentValue() != null) {
            goal.setCurrentValue(request.getCurrentValue());
        }
        goal.setUnit(request.getUnit());
        goal.setStartDate(request.getStartDate());
        goal.setTargetDate(request.getTargetDate());

        return mapToResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthGoalResponse> getGoals(Long patientId) {
        return goalRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteGoal(Long patientId, Long goalId) {
        HealthGoal goal = goalRepository.findByIdAndPatientId(goalId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthGoal", "id", goalId));
        goalRepository.delete(goal);
    }

    private HealthGoalResponse mapToResponse(HealthGoal g) {
        BigDecimal progress = BigDecimal.ZERO;
        if (g.getTargetValue() != null && g.getTargetValue().compareTo(BigDecimal.ZERO) > 0 && g.getCurrentValue() != null) {
            progress = g.getCurrentValue()
                    .divide(g.getTargetValue(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);
        }

        return HealthGoalResponse.builder()
                .id(g.getId())
                .patientId(g.getPatientId())
                .goalType(g.getGoalType())
                .targetValue(g.getTargetValue())
                .currentValue(g.getCurrentValue())
                .progressPercentage(progress)
                .unit(g.getUnit())
                .startDate(g.getStartDate())
                .targetDate(g.getTargetDate())
                .status(g.getStatus())
                .createdAt(g.getCreatedAt())
                .updatedAt(g.getUpdatedAt())
                .build();
    }
}
