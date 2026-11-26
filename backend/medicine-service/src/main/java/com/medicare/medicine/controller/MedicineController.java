package com.medicare.medicine.controller;

import com.medicare.common.dto.ApiResponse;
import com.medicare.medicine.dto.MedicineRequest;
import com.medicare.medicine.dto.MedicineResponse;
import com.medicare.medicine.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
@Tag(name = "Medicines", description = "Medicine management, dosages, and patient medication listings")
public class MedicineController {

    private final MedicineService medicineService;

    @PostMapping
    @Operation(summary = "Add a new medicine for a patient")
    public ResponseEntity<ApiResponse<MedicineResponse>> addMedicine(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId,
            @Valid @RequestBody MedicineRequest request) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        if (patientId == null) {
            patientId = 1L; // Fallback
        }
        MedicineResponse response = medicineService.addMedicine(patientId, request);
        return new ResponseEntity<>(ApiResponse.success(response, "Medicine added successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing medicine")
    public ResponseEntity<ApiResponse<MedicineResponse>> updateMedicine(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId,
            @Valid @RequestBody MedicineRequest request) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        MedicineResponse response = medicineService.updateMedicine(patientId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Medicine updated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medicine details by ID")
    public ResponseEntity<ApiResponse<MedicineResponse>> getMedicineById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        MedicineResponse response = medicineService.getMedicineById(patientId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete / deactivate a medicine")
    public ResponseEntity<ApiResponse<Void>> deleteMedicine(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        medicineService.deleteMedicine(patientId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Medicine deleted successfully"));
    }

    @GetMapping("/active")
    @Operation(summary = "List all active medicines for the patient")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getActiveMedicines(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        List<MedicineResponse> list = medicineService.getActiveMedicines(patientId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping
    @Operation(summary = "Search and filter patient medicines with pagination")
    public ResponseEntity<ApiResponse<Page<MedicineResponse>>> searchMedicines(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Page<MedicineResponse> results = medicineService.searchMedicines(patientId, keyword, categoryId, isActive, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/count")
    @Operation(summary = "Get count of active medicines for patient")
    public ResponseEntity<ApiResponse<Long>> countActive(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        long count = medicineService.countActiveMedicines(patientId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
