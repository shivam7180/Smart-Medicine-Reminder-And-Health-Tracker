package com.medicare.health.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class HealthGoalRequest {
    @NotBlank(message = "Goal type is required (WEIGHT, WATER_INTAKE, SLEEP, STEPS, etc.)")
    private String goalType;

    @NotNull(message = "Target value is required")
    private BigDecimal targetValue;

    private BigDecimal currentValue;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate targetDate;
}
