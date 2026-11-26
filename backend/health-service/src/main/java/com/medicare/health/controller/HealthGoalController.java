package com.medicare.health.controller;

import com.medicare.common.dto.ApiResponse;
import com.medicare.health.dto.HealthGoalRequest;
import com.medicare.health.dto.HealthGoalResponse;
import com.medicare.health.service.HealthGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
@Tag(name = "Health Goals", description = "Track fitness goals (weight target, water intake, sleep, activity)")
public class HealthGoalController {

    private final HealthGoalService goalService;

    @PostMapping
    @Operation(summary = "Create a new personal health goal")
    public ResponseEntity<ApiResponse<HealthGoalResponse>> createGoal(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId,
            @Valid @RequestBody HealthGoalRequest request) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        if (patientId == null) patientId = 1L;
        return new ResponseEntity<>(ApiResponse.success(goalService.createGoal(patientId, request), "Goal created"), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List all active health goals for patient")
    public ResponseEntity<ApiResponse<List<HealthGoalResponse>>> getGoals(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(goalService.getGoals(patientId)));
    }

    @PatchMapping("/{id}/progress")
    @Operation(summary = "Update current progress value for a goal")
    public ResponseEntity<ApiResponse<HealthGoalResponse>> updateProgress(
            @PathVariable Long id,
            @RequestParam BigDecimal currentValue,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(goalService.updateGoalProgress(patientId, id, currentValue), "Goal progress updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete goal")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        goalService.deleteGoal(patientId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Goal deleted successfully"));
    }
}
