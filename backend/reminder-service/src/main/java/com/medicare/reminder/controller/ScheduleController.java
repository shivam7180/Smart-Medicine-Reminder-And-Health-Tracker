package com.medicare.reminder.controller;

import com.medicare.common.dto.ApiResponse;
import com.medicare.reminder.dto.ScheduleRequest;
import com.medicare.reminder.dto.ScheduleResponse;
import com.medicare.reminder.service.MedicineScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "Medicine Schedules", description = "Setup daily, weekly, and custom reminder schedules")
public class ScheduleController {

    private final MedicineScheduleService scheduleService;

    @PostMapping
    @Operation(summary = "Create medicine reminder schedule")
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId,
            @Valid @RequestBody ScheduleRequest request) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        if (patientId == null) patientId = 1L;
        return new ResponseEntity<>(ApiResponse.success(scheduleService.createSchedule(patientId, request), "Schedule created successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update reminder schedule")
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId,
            @Valid @RequestBody ScheduleRequest request) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(scheduleService.updateSchedule(patientId, id, request), "Schedule updated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get schedule details by ID")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getScheduleById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getScheduleById(patientId, id)));
    }

    @GetMapping("/active")
    @Operation(summary = "List active schedules for patient")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getActiveSchedules(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getActiveSchedules(patientId)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle active status of schedule")
    public ResponseEntity<ApiResponse<Void>> toggleScheduleStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        scheduleService.toggleScheduleStatus(patientId, id, active);
        return ResponseEntity.ok(ApiResponse.success(null, "Schedule status updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate schedule")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        scheduleService.deleteSchedule(patientId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Schedule removed successfully"));
    }
}
