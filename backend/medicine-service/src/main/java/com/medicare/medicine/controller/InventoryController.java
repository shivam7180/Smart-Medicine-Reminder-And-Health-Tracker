package com.medicare.medicine.controller;

import com.medicare.common.dto.ApiResponse;
import com.medicare.medicine.dto.InventoryDto;
import com.medicare.medicine.dto.LowStockAlertDto;
import com.medicare.medicine.dto.StockAdjustmentRequest;
import com.medicare.medicine.service.MedicineInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Medicine Inventory", description = "Stock tracking, refills, thresholds, and low-stock alarms")
public class InventoryController {

    private final MedicineInventoryService inventoryService;

    @GetMapping("/medicine/{medicineId}")
    @Operation(summary = "Get current inventory information for a medicine")
    public ResponseEntity<ApiResponse<InventoryDto>> getInventoryByMedicineId(@PathVariable Long medicineId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventoryByMedicineId(medicineId)));
    }

    @PostMapping("/medicine/{medicineId}/adjust")
    @Operation(summary = "Adjust current stock (restock or manual deduction)")
    public ResponseEntity<ApiResponse<InventoryDto>> adjustStock(
            @PathVariable Long medicineId,
            @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.adjustStock(medicineId, request), "Stock updated successfully"));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get all low-stock medicines for a patient")
    public ResponseEntity<ApiResponse<List<LowStockAlertDto>>> getLowStock(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getLowStockAlerts(patientId)));
    }

    @PostMapping("/internal/deduct")
    @Operation(summary = "Internal API: Deduct medicine dose from stock upon dose TAKEN event")
    public ResponseEntity<Void> deductStockInternal(
            @RequestParam Long medicineId,
            @RequestParam(defaultValue = "1") int quantity) {
        inventoryService.deductDoseFromStock(medicineId, quantity);
        return ResponseEntity.ok().build();
    }
}
