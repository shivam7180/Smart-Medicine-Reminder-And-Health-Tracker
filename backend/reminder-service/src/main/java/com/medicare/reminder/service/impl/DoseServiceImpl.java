package com.medicare.reminder.service.impl;

import com.medicare.common.enums.DoseStatus;
import com.medicare.common.exception.BadRequestException;
import com.medicare.common.exception.ResourceNotFoundException;
import com.medicare.reminder.client.MedicineInventoryClient;
import com.medicare.reminder.client.NotificationClient;
import com.medicare.reminder.dto.AdherenceStatsDto;
import com.medicare.reminder.dto.DoseLogResponse;
import com.medicare.reminder.dto.TodayRemindersSummaryDto;
import com.medicare.reminder.entity.DoseLog;
import com.medicare.reminder.entity.MedicineSchedule;
import com.medicare.reminder.entity.ScheduleTime;
import com.medicare.reminder.repository.DoseLogRepository;
import com.medicare.reminder.repository.MedicineScheduleRepository;
import com.medicare.reminder.service.DoseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoseServiceImpl implements DoseService {

    private final DoseLogRepository doseLogRepository;
    private final MedicineScheduleRepository scheduleRepository;
    private final MedicineInventoryClient inventoryClient;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public DoseLogResponse takeMedicine(Long patientId, Long doseId) {
        DoseLog dose = doseLogRepository.findByIdAndPatientId(doseId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("DoseLog", "id", doseId));

        if (dose.getStatus() == DoseStatus.TAKEN) {
            throw new BadRequestException("This dose has already been marked as taken");
        }

        dose.setStatus(DoseStatus.TAKEN);
        dose.setActualTime(LocalDateTime.now());
        DoseLog saved = doseLogRepository.save(dose);

        // Deduct inventory asynchronously/via Feign
        try {
            inventoryClient.deductStock(saved.getMedicineId(), 1);
        } catch (Exception e) {
            log.error("Failed to deduct inventory for medicine id {}: {}", saved.getMedicineId(), e.getMessage());
        }

        // Notify patient of success
        try {
            notificationClient.sendNotification(
                    patientId,
                    "Dose Confirmed",
                    "You successfully took " + saved.getMedicineName() + " at " + LocalTime.now().withSecond(0),
                    "MEDICINE_REMINDER",
                    saved.getId()
            );
        } catch (Exception e) {
            log.warn("Could not dispatch notification: {}", e.getMessage());
        }

        log.info("Dose {} marked as TAKEN by patient {}", doseId, patientId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DoseLogResponse skipMedicine(Long patientId, Long doseId, String reason) {
        DoseLog dose = doseLogRepository.findByIdAndPatientId(doseId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("DoseLog", "id", doseId));

        dose.setStatus(DoseStatus.SKIPPED);
        dose.setSkipReason(reason != null && !reason.isBlank() ? reason : "Skipped by user");
        dose.setActualTime(LocalDateTime.now());
        DoseLog saved = doseLogRepository.save(dose);

        log.info("Dose {} marked as SKIPPED by patient {}", doseId, patientId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DoseLogResponse snoozeMedicine(Long patientId, Long doseId, Integer minutes) {
        DoseLog dose = doseLogRepository.findByIdAndPatientId(doseId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("DoseLog", "id", doseId));

        int snoozeMin = (minutes != null && minutes > 0) ? minutes : 15;
        dose.setStatus(DoseStatus.SNOOZED);
        dose.setSnoozeCount(dose.getSnoozeCount() + 1);
        dose.setScheduledTime(LocalDateTime.now().plusMinutes(snoozeMin));

        DoseLog saved = doseLogRepository.save(dose);
        log.info("Dose {} snoozed for {} minutes by patient {}", doseId, snoozeMin, patientId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoseLogResponse> getTodayDoses(Long patientId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        return doseLogRepository.findByPatientIdAndScheduledTimeBetweenOrderByScheduledTimeAsc(patientId, startOfDay, endOfDay)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TodayRemindersSummaryDto getTodaySummary(Long patientId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<DoseLog> todayList = doseLogRepository.findByPatientIdAndScheduledTimeBetweenOrderByScheduledTimeAsc(patientId, startOfDay, endOfDay);

        long total = todayList.size();
        long taken = todayList.stream().filter(d -> d.getStatus() == DoseStatus.TAKEN).count();
        long skipped = todayList.stream().filter(d -> d.getStatus() == DoseStatus.SKIPPED).count();
        long missed = todayList.stream().filter(d -> d.getStatus() == DoseStatus.MISSED).count();
        long pending = todayList.stream().filter(d -> d.getStatus() == DoseStatus.SCHEDULED || d.getStatus() == DoseStatus.SNOOZED).count();

        BigDecimal adherenceRate = BigDecimal.ZERO;
        if (total > 0) {
            adherenceRate = BigDecimal.valueOf((double) taken / total * 100.0)
                    .setScale(1, RoundingMode.HALF_UP);
        }

        // Find next upcoming reminder
        LocalDateTime now = LocalDateTime.now();
        DoseLogResponse nextUpcoming = todayList.stream()
                .filter(d -> (d.getStatus() == DoseStatus.SCHEDULED || d.getStatus() == DoseStatus.SNOOZED)
                        && d.getScheduledTime().isAfter(now))
                .min(Comparator.comparing(DoseLog::getScheduledTime))
                .map(this::mapToResponse)
                .orElse(null);

        List<DoseLogResponse> doseResponses = todayList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return TodayRemindersSummaryDto.builder()
                .totalScheduledToday(total)
                .takenToday(taken)
                .skippedToday(skipped)
                .missedToday(missed)
                .pendingToday(pending)
                .adherenceRateToday(adherenceRate)
                .nextUpcomingReminder(nextUpcoming)
                .todayDoses(doseResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoseLogResponse> getDoseHistory(Long patientId, Pageable pageable) {
        return doseLogRepository.findByPatientIdOrderByScheduledTimeDesc(patientId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoseLogResponse> getMissedDoses(Long patientId) {
        return doseLogRepository.findByPatientIdAndStatusOrderByScheduledTimeDesc(patientId, DoseStatus.MISSED)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdherenceStatsDto calculateAdherence(Long patientId, int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        long total = doseLogRepository.countTotalInRange(patientId, startTime, endTime);
        long taken = doseLogRepository.countTakenInRange(patientId, startTime, endTime);

        List<DoseLog> list = doseLogRepository.findByPatientIdAndScheduledTimeBetweenOrderByScheduledTimeAsc(patientId, startTime, endTime);
        long skipped = list.stream().filter(d -> d.getStatus() == DoseStatus.SKIPPED).count();
        long missed = list.stream().filter(d -> d.getStatus() == DoseStatus.MISSED).count();

        BigDecimal adherence = BigDecimal.ZERO;
        if (total > 0) {
            adherence = BigDecimal.valueOf((double) taken / total * 100.0)
                    .setScale(1, RoundingMode.HALF_UP);
        }

        return AdherenceStatsDto.builder()
                .patientId(patientId)
                .totalScheduled(total)
                .takenCount(taken)
                .skippedCount(skipped)
                .missedCount(missed)
                .adherencePercentage(adherence)
                .build();
    }

    @Override
    @Transactional
    public int generateDosesForDate(LocalDate date) {
        List<MedicineSchedule> schedules = scheduleRepository.findActiveSchedulesForDate(date);
        int generatedCount = 0;

        for (MedicineSchedule schedule : schedules) {
            for (ScheduleTime st : schedule.getReminderTimes()) {
                LocalDateTime doseTime = LocalDateTime.of(date, st.getReminderTime());

                // Guard against duplicate doses
                if (!doseLogRepository.existsByScheduleIdAndScheduledTime(schedule.getId(), doseTime)) {
                    DoseLog logEntry = DoseLog.builder()
                            .scheduleId(schedule.getId())
                            .patientId(schedule.getPatientId())
                            .medicineId(schedule.getMedicineId())
                            .medicineName(schedule.getMedicineName())
                            .scheduledTime(doseTime)
                            .status(DoseStatus.SCHEDULED)
                            .snoozeCount(0)
                            .build();

                    doseLogRepository.save(logEntry);
                    generatedCount++;
                }
            }
        }

        log.info("Generated {} scheduled doses for date {}", generatedCount, date);
        return generatedCount;
    }

    private DoseLogResponse mapToResponse(DoseLog d) {
        return DoseLogResponse.builder()
                .id(d.getId())
                .scheduleId(d.getScheduleId())
                .patientId(d.getPatientId())
                .medicineId(d.getMedicineId())
                .medicineName(d.getMedicineName())
                .scheduledTime(d.getScheduledTime())
                .actualTime(d.getActualTime())
                .status(d.getStatus())
                .skipReason(d.getSkipReason())
                .snoozeCount(d.getSnoozeCount())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
