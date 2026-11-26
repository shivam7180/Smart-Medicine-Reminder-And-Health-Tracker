package com.medicare.health.controller;

import com.medicare.common.dto.ApiResponse;
import com.medicare.health.dto.HealthRecordRequest;
import com.medicare.health.dto.HealthRecordResponse;
import com.medicare.health.dto.LatestVitalsDto;
import com.medicare.health.service.HealthRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health Tracking", description = "Record, monitor, and visualize vital biometrics (BP, Sugar, Heart Rate, BMI, SpO2, etc.)")
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    @PostMapping("/records")
    @Operation(summary = "Record a new vital sign measurement (auto-computes BMI if height/weight)")
    public ResponseEntity<ApiResponse<HealthRecordResponse>> addRecord(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId,
            @Valid @RequestBody HealthRecordRequest request) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        if (patientId == null) patientId = 1L;
        HealthRecordResponse response = healthRecordService.addRecord(patientId, request);
        return new ResponseEntity<>(ApiResponse.success(response, "Health reading saved successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/records/{id}")
    @Operation(summary = "Update an existing health record")
    public ResponseEntity<ApiResponse<HealthRecordResponse>> updateRecord(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId,
            @Valid @RequestBody HealthRecordRequest request) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(healthRecordService.updateRecord(patientId, id, request)));
    }

    @GetMapping("/records/{id}")
    @Operation(summary = "Get health record by ID")
    public ResponseEntity<ApiResponse<HealthRecordResponse>> getRecordById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(healthRecordService.getRecordById(patientId, id)));
    }

    @DeleteMapping("/records/{id}")
    @Operation(summary = "Delete health record")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        healthRecordService.deleteRecord(patientId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Health record deleted successfully"));
    }

    @GetMapping("/history")
    @Operation(summary = "Get paginated history of health readings, with optional filter by recordType")
    public ResponseEntity<ApiResponse<Page<HealthRecordResponse>>> getHistory(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId,
            @RequestParam(value = "recordType", required = false) String recordType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(healthRecordService.getRecordHistory(patientId, recordType, PageRequest.of(page, size))));
    }

    @GetMapping("/trends")
    @Operation(summary = "Get trend data for charts (e.g. BLOOD_PRESSURE, BLOOD_SUGAR) over last N days")
    public ResponseEntity<ApiResponse<List<HealthRecordResponse>>> getTrends(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId,
            @RequestParam String recordType,
            @RequestParam(defaultValue = "30") int days) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(healthRecordService.getTrendData(patientId, recordType, days)));
    }

    @GetMapping("/vitals/latest")
    @Operation(summary = "Get latest snapshot of all vitals for patient dashboard")
    public ResponseEntity<ApiResponse<LatestVitalsDto>> getLatestVitals(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(healthRecordService.getLatestVitals(patientId)));
    }
}
