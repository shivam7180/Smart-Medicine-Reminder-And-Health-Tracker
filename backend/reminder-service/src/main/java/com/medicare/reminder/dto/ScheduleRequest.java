package com.medicare.reminder.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class ScheduleRequest {
    @NotNull(message = "Medicine ID is required")
    private Long medicineId;

    @NotNull(message = "Medicine name is required")
    private String medicineName;

    private String dosageQuantity = "1";
    private String frequencyType = "DAILY"; // DAILY, WEEKLY, CUSTOM

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;
    private String timeZone = "UTC";

    @NotEmpty(message = "At least one reminder time must be configured")
    private List<LocalTime> reminderTimes;
}
