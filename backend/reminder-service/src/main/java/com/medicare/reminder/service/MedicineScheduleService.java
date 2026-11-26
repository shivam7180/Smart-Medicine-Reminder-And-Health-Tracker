package com.medicare.reminder.service;

import com.medicare.reminder.dto.ScheduleRequest;
import com.medicare.reminder.dto.ScheduleResponse;

import java.util.List;

public interface MedicineScheduleService {
    ScheduleResponse createSchedule(Long patientId, ScheduleRequest request);
    ScheduleResponse updateSchedule(Long patientId, Long scheduleId, ScheduleRequest request);
    ScheduleResponse getScheduleById(Long patientId, Long scheduleId);
    List<ScheduleResponse> getActiveSchedules(Long patientId);
    void toggleScheduleStatus(Long patientId, Long scheduleId, boolean active);
    void deleteSchedule(Long patientId, Long scheduleId);
}
