package com.medicare.health.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatestVitalsDto {
    private BigDecimal bloodPressureSystolic;
    private BigDecimal bloodPressureDiastolic;
    private BigDecimal bloodSugar;
    private BigDecimal heartRate;
    private BigDecimal temperature;
    private BigDecimal weight;
    private BigDecimal height;
    private BigDecimal bmi;
    private String bmiCategory; // UNDERWEIGHT, NORMAL, OVERWEIGHT, OBESE
    private BigDecimal spo2;
    private BigDecimal waterIntake;
    private BigDecimal sleepHours;
}
