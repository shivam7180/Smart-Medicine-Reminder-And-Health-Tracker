package com.medicare.health.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecordResponse {
    private Long id;
    private Long patientId;
    private String recordType;
    private BigDecimal valuePrimary;
    private BigDecimal valueSecondary;
    private String unit;
    private LocalDateTime readingTime;
    private String notes;
    private Boolean abnormalFlag;
    private String warningMessage;
    private LocalDateTime createdAt;
}
