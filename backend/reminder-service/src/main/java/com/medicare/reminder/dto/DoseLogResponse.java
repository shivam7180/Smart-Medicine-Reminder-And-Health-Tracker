package com.medicare.reminder.dto;

import com.medicare.common.enums.DoseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoseLogResponse {
    private Long id;
    private Long scheduleId;
    private Long patientId;
    private Long medicineId;
    private String medicineName;
    private LocalDateTime scheduledTime;
    private LocalDateTime actualTime;
    private DoseStatus status; // SCHEDULED, TAKEN, SKIPPED, MISSED, SNOOZED
    private String skipReason;
    private Integer snoozeCount;
    private LocalDateTime createdAt;
}
