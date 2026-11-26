package com.medicare.medicine.service;

import com.medicare.medicine.dto.InventoryDto;
import com.medicare.medicine.dto.LowStockAlertDto;
import com.medicare.medicine.dto.StockAdjustmentRequest;

import java.util.List;

public interface MedicineInventoryService {
    InventoryDto getInventoryByMedicineId(Long medicineId);
    InventoryDto adjustStock(Long medicineId, StockAdjustmentRequest request);
    List<LowStockAlertDto> getLowStockAlerts(Long patientId);
    void deductDoseFromStock(Long medicineId, int quantity);
}
