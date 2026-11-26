package com.medicare.health.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthGoalResponse {
    private Long id;
    private Long patientId;
    private String goalType;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private BigDecimal progressPercentage;
    private String unit;
    private LocalDate startDate;
    private LocalDate targetDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
