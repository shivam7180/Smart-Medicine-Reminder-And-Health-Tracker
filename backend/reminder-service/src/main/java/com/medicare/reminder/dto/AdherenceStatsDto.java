package com.medicare.reminder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdherenceStatsDto {
    private Long patientId;
    private long totalScheduled;
    private long takenCount;
    private long skippedCount;
    private long missedCount;
    private BigDecimal adherencePercentage; // (taken / total) * 100
}
