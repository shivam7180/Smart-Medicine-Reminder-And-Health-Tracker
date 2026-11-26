package com.medicare.health.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HealthRecordRequest {
    @NotBlank(message = "Record type is required (e.g. BLOOD_PRESSURE, BLOOD_SUGAR, HEART_RATE, WEIGHT, HEIGHT, etc.)")
    private String recordType;

    @NotNull(message = "Primary numeric value is required")
    private BigDecimal valuePrimary;

    private BigDecimal valueSecondary; // e.g. Diastolic for BP

    @NotBlank(message = "Unit is required (e.g. mmHg, mg/dL, bpm, kg, etc.)")
    private String unit;

    private LocalDateTime readingTime; // defaults to now if null
    private String notes;
}
