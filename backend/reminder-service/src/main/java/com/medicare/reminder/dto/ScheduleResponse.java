package com.medicare.reminder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private Long id;
    private Long patientId;
    private Long medicineId;
    private String medicineName;
    private String dosageQuantity;
    private String frequencyType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private String timeZone;
    private List<LocalTime> reminderTimes;
    private LocalDateTime createdAt;
}
