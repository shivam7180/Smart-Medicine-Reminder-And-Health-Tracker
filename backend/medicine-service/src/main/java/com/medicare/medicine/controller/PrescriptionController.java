package com.medicare.medicine.controller;

import com.medicare.common.dto.ApiResponse;
import com.medicare.medicine.dto.PrescriptionRequest;
import com.medicare.medicine.dto.PrescriptionResponse;
import com.medicare.medicine.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Prescriptions", description = "Prescription archiving, uploads, and doctor-patient orders")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @Operation(summary = "Create/upload a new prescription")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> createPrescription(@Valid @RequestBody PrescriptionRequest request) {
        return new ResponseEntity<>(ApiResponse.success(prescriptionService.createPrescription(request), "Prescription recorded successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List prescriptions for patient")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getPrescriptions(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getPrescriptionsByPatient(patientId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get prescription by ID")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getPrescriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getPrescriptionById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete prescription")
    public ResponseEntity<ApiResponse<Void>> deletePrescription(@PathVariable Long id) {
        prescriptionService.deletePrescription(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Prescription removed successfully"));
    }
}
