package com.medicare.reminder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayRemindersSummaryDto {
    private long totalScheduledToday;
    private long takenToday;
    private long skippedToday;
    private long missedToday;
    private long pendingToday;
    private BigDecimal adherenceRateToday;
    private DoseLogResponse nextUpcomingReminder;
    private List<DoseLogResponse> todayDoses;
}
