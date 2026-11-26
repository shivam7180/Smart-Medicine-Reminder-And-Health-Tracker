package com.medicare.analytics.controller;

import com.medicare.analytics.service.AnalyticsService;
import com.medicare.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics & Dashboard Aggregations", description = "Composite dashboard metrics, adherence trends, and patient vitals charts")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard/patient")
    @Operation(summary = "Get unified patient dashboard overview (active meds, today doses, adherence, vitals, upcoming appointments)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPatientDashboard(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        if (patientId == null) patientId = 1L;
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getPatientDashboardSummary(patientId)));
    }

    @GetMapping("/adherence/summary")
    @Operation(summary = "Get full adherence analytics breakdown")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdherenceSummary(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId,
            @RequestParam(defaultValue = "30") int days) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getComprehensiveAdherenceReport(patientId, days)));
    }
}
