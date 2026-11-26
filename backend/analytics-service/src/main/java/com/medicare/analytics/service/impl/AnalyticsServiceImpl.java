package com.medicare.analytics.service.impl;

import com.medicare.analytics.client.AppointmentFeignClient;
import com.medicare.analytics.client.HealthFeignClient;
import com.medicare.analytics.client.MedicineFeignClient;
import com.medicare.analytics.client.ReminderFeignClient;
import com.medicare.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final MedicineFeignClient medicineClient;
    private final ReminderFeignClient reminderClient;
    private final HealthFeignClient healthClient;
    private final AppointmentFeignClient appointmentClient;

    @Override
    public Map<String, Object> getPatientDashboardSummary(Long patientId) {
        Map<String, Object> summary = new HashMap<>();

        // 1. Total Active Medicines
        try {
            Long activeCount = medicineClient.countActive(patientId).getData();
            summary.put("activeMedicinesCount", activeCount != null ? activeCount : 0L);
        } catch (Exception e) {
            log.warn("Error fetching active medicines count: {}", e.getMessage());
            summary.put("activeMedicinesCount", 0L);
        }

        // 2. Today's Reminders Summary
        try {
            Map<String, Object> todayData = reminderClient.getTodaySummary(patientId).getData();
            summary.put("todayReminders", todayData);
        } catch (Exception e) {
            log.warn("Error fetching today reminders: {}", e.getMessage());
            summary.put("todayReminders", Map.of(
                    "totalScheduledToday", 0,
                    "takenToday", 0,
                    "skippedToday", 0,
                    "missedToday", 0,
                    "adherenceRateToday", 0
            ));
        }

        // 3. 30-Day Adherence Percentage
        try {
            Map<String, Object> adherenceData = reminderClient.getAdherence(patientId, 30).getData();
            summary.put("adherenceStats", adherenceData);
        } catch (Exception e) {
            log.warn("Error fetching adherence: {}", e.getMessage());
            summary.put("adherenceStats", Map.of("adherencePercentage", 0));
        }

        // 4. Latest Health Vitals (BP, Sugar, HR, BMI, etc.)
        try {
            Map<String, Object> vitals = healthClient.getLatestVitals(patientId).getData();
            summary.put("latestVitals", vitals);
        } catch (Exception e) {
            log.warn("Error fetching latest vitals: {}", e.getMessage());
            summary.put("latestVitals", Map.of());
        }

        // 5. Blood Pressure Trend (Last 7 days)
        try {
            List<Map<String, Object>> bpTrends = healthClient.getTrends(patientId, "BLOOD_PRESSURE", 7).getData();
            summary.put("bloodPressureTrends", bpTrends);
        } catch (Exception e) {
            summary.put("bloodPressureTrends", List.of());
        }

        // 6. Blood Sugar Trend (Last 7 days)
        try {
            List<Map<String, Object>> sugarTrends = healthClient.getTrends(patientId, "BLOOD_SUGAR", 7).getData();
            summary.put("bloodSugarTrends", sugarTrends);
        } catch (Exception e) {
            summary.put("bloodSugarTrends", List.of());
        }

        // 7. Upcoming Appointments
        try {
            List<Map<String, Object>> appointments = appointmentClient.getUpcomingAppointments(patientId).getData();
            summary.put("upcomingAppointments", appointments);
        } catch (Exception e) {
            summary.put("upcomingAppointments", List.of());
        }

        return summary;
    }

    @Override
    public Map<String, Object> getComprehensiveAdherenceReport(Long patientId, int days) {
        Map<String, Object> report = new HashMap<>();
        try {
            report.put("adherence", reminderClient.getAdherence(patientId, days).getData());
            report.put("medicines", medicineClient.getActiveMedicines(patientId).getData());
            report.put("latestVitals", healthClient.getLatestVitals(patientId).getData());
        } catch (Exception e) {
            log.error("Failed to build adherence report: {}", e.getMessage());
        }
        return report;
    }
}
