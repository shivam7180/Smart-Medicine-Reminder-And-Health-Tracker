package com.medicare.reminder.service.impl;

import com.medicare.common.exception.ResourceNotFoundException;
import com.medicare.reminder.dto.ScheduleRequest;
import com.medicare.reminder.dto.ScheduleResponse;
import com.medicare.reminder.entity.MedicineSchedule;
import com.medicare.reminder.entity.ScheduleTime;
import com.medicare.reminder.repository.MedicineScheduleRepository;
import com.medicare.reminder.service.DoseService;
import com.medicare.reminder.service.MedicineScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineScheduleServiceImpl implements MedicineScheduleService {

    private final MedicineScheduleRepository scheduleRepository;

    @Override
    @Transactional
    public ScheduleResponse createSchedule(Long patientId, ScheduleRequest request) {
        MedicineSchedule schedule = MedicineSchedule.builder()
                .patientId(patientId)
                .medicineId(request.getMedicineId())
                .medicineName(request.getMedicineName().trim())
                .dosageQuantity(request.getDosageQuantity() != null ? request.getDosageQuantity() : "1")
                .frequencyType(request.getFrequencyType() != null ? request.getFrequencyType() : "DAILY")
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(true)
                .timeZone(request.getTimeZone() != null ? request.getTimeZone() : "UTC")
                .build();

        if (request.getReminderTimes() != null) {
            for (LocalTime time : request.getReminderTimes()) {
                ScheduleTime st = ScheduleTime.builder()
                        .reminderTime(time)
                        .schedule(schedule)
                        .build();
                schedule.addReminderTime(st);
            }
        }

        MedicineSchedule saved = scheduleRepository.save(schedule);
        log.info("Created new medicine schedule {} for patient {}", saved.getId(), patientId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ScheduleResponse updateSchedule(Long patientId, Long scheduleId, ScheduleRequest request) {
        MedicineSchedule schedule = scheduleRepository.findByIdAndPatientId(scheduleId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", scheduleId));

        schedule.setMedicineName(request.getMedicineName());
        schedule.setDosageQuantity(request.getDosageQuantity());
        schedule.setFrequencyType(request.getFrequencyType());
        schedule.setStartDate(request.getStartDate());
        schedule.setEndDate(request.getEndDate());
        schedule.setTimeZone(request.getTimeZone());

        // Update reminder times
        schedule.getReminderTimes().clear();
        if (request.getReminderTimes() != null) {
            for (LocalTime time : request.getReminderTimes()) {
                ScheduleTime st = ScheduleTime.builder()
                        .reminderTime(time)
                        .schedule(schedule)
                        .build();
                schedule.addReminderTime(st);
            }
        }

        MedicineSchedule updated = scheduleRepository.save(schedule);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleResponse getScheduleById(Long patientId, Long scheduleId) {
        MedicineSchedule schedule = scheduleRepository.findByIdAndPatientId(scheduleId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", scheduleId));
        return mapToResponse(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getActiveSchedules(Long patientId) {
        return scheduleRepository.findByPatientIdAndIsActiveTrue(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void toggleScheduleStatus(Long patientId, Long scheduleId, boolean active) {
        MedicineSchedule schedule = scheduleRepository.findByIdAndPatientId(scheduleId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", scheduleId));
        schedule.setIsActive(active);
        scheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long patientId, Long scheduleId) {
        MedicineSchedule schedule = scheduleRepository.findByIdAndPatientId(scheduleId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", scheduleId));
        schedule.setIsActive(false);
        scheduleRepository.save(schedule);
    }

    private ScheduleResponse mapToResponse(MedicineSchedule s) {
        List<LocalTime> times = s.getReminderTimes().stream()
                .map(ScheduleTime::getReminderTime)
                .sorted()
                .collect(Collectors.toList());

        return ScheduleResponse.builder()
                .id(s.getId())
                .patientId(s.getPatientId())
                .medicineId(s.getMedicineId())
                .medicineName(s.getMedicineName())
                .dosageQuantity(s.getDosageQuantity())
                .frequencyType(s.getFrequencyType())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .isActive(s.getIsActive())
                .timeZone(s.getTimeZone())
                .reminderTimes(times)
                .createdAt(s.getCreatedAt())
                .build();
    }
}
