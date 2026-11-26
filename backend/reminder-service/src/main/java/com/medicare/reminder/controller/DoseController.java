package com.medicare.reminder.controller;

import com.medicare.common.dto.ApiResponse;
import com.medicare.reminder.dto.AdherenceStatsDto;
import com.medicare.reminder.dto.DoseActionRequest;
import com.medicare.reminder.dto.DoseLogResponse;
import com.medicare.reminder.dto.TodayRemindersSummaryDto;
import com.medicare.reminder.service.DoseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/doses")
@RequiredArgsConstructor
@Tag(name = "Dose Execution & Reminders", description = "Take, skip, snooze reminders, track adherence, and fetch daily summaries")
public class DoseController {

    private final DoseService doseService;

    @PostMapping("/{id}/take")
    @Operation(summary = "Mark a dose as TAKEN (deducts stock and updates adherence)")
    public ResponseEntity<ApiResponse<DoseLogResponse>> takeMedicine(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        DoseLogResponse response = doseService.takeMedicine(patientId, id);
        return ResponseEntity.ok(ApiResponse.success(response, "Medicine dose recorded as taken"));
    }

    @PostMapping("/{id}/skip")
    @Operation(summary = "Mark a dose as SKIPPED with an optional reason")
    public ResponseEntity<ApiResponse<DoseLogResponse>> skipMedicine(
            @PathVariable Long id,
            @RequestBody(required = false) DoseActionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        String reason = (request != null) ? request.getReason() : "Skipped by user";
        DoseLogResponse response = doseService.skipMedicine(patientId, id, reason);
        return ResponseEntity.ok(ApiResponse.success(response, "Medicine dose marked as skipped"));
    }

    @PostMapping("/{id}/snooze")
    @Operation(summary = "Snooze a dose by specified minutes (defaults to 15 mins)")
    public ResponseEntity<ApiResponse<DoseLogResponse>> snoozeMedicine(
            @PathVariable Long id,
            @RequestBody(required = false) DoseActionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        Integer minutes = (request != null) ? request.getSnoozeMinutes() : 15;
        DoseLogResponse response = doseService.snoozeMedicine(patientId, id, minutes);
        return ResponseEntity.ok(ApiResponse.success(response, "Reminder snoozed for " + minutes + " minutes"));
    }

    @GetMapping("/today")
    @Operation(summary = "Get list of all scheduled doses for today")
    public ResponseEntity<ApiResponse<List<DoseLogResponse>>> getTodayDoses(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(doseService.getTodayDoses(patientId)));
    }

    @GetMapping("/today/summary")
    @Operation(summary = "Get today's reminder summary: scheduled, taken, skipped, missed, next upcoming dose, and today's adherence")
    public ResponseEntity<ApiResponse<TodayRemindersSummaryDto>> getTodaySummary(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(doseService.getTodaySummary(patientId)));
    }

    @GetMapping("/history")
    @Operation(summary = "Get dose adherence history with pagination")
    public ResponseEntity<ApiResponse<Page<DoseLogResponse>>> getDoseHistory(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(doseService.getDoseHistory(patientId, PageRequest.of(page, size))));
    }

    @GetMapping("/missed")
    @Operation(summary = "Get list of all missed doses")
    public ResponseEntity<ApiResponse<List<DoseLogResponse>>> getMissedDoses(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(doseService.getMissedDoses(patientId)));
    }

    @GetMapping("/adherence")
    @Operation(summary = "Get medicine adherence statistics for past N days (defaults to 30 days)")
    public ResponseEntity<ApiResponse<AdherenceStatsDto>> getAdherence(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId,
            @RequestParam(defaultValue = "30") int days) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(doseService.calculateAdherence(patientId, days)));
    }

    @PostMapping("/generate")
    @Operation(summary = "Trigger dose generation for date (defaults to today)")
    public ResponseEntity<ApiResponse<Integer>> generateDoses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        int count = doseService.generateDosesForDate(target);
        return ResponseEntity.ok(ApiResponse.success(count, "Generated " + count + " doses for " + target));
    }
}
