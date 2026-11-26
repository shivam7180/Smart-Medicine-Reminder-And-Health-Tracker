package com.medicare.reminder.service;

import com.medicare.reminder.dto.AdherenceStatsDto;
import com.medicare.reminder.dto.DoseLogResponse;
import com.medicare.reminder.dto.TodayRemindersSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface DoseService {
    DoseLogResponse takeMedicine(Long patientId, Long doseId);
    DoseLogResponse skipMedicine(Long patientId, Long doseId, String reason);
    DoseLogResponse snoozeMedicine(Long patientId, Long doseId, Integer minutes);
    List<DoseLogResponse> getTodayDoses(Long patientId);
    TodayRemindersSummaryDto getTodaySummary(Long patientId);
    Page<DoseLogResponse> getDoseHistory(Long patientId, Pageable pageable);
    List<DoseLogResponse> getMissedDoses(Long patientId);
    AdherenceStatsDto calculateAdherence(Long patientId, int days);
    int generateDosesForDate(LocalDate date);
}
