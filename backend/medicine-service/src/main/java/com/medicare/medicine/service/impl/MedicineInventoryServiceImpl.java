package com.medicare.medicine.service.impl;

import com.medicare.common.exception.ResourceNotFoundException;
import com.medicare.medicine.dto.InventoryDto;
import com.medicare.medicine.dto.LowStockAlertDto;
import com.medicare.medicine.dto.StockAdjustmentRequest;
import com.medicare.medicine.entity.MedicineInventory;
import com.medicare.medicine.repository.MedicineInventoryRepository;
import com.medicare.medicine.service.MedicineInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineInventoryServiceImpl implements MedicineInventoryService {

    private final MedicineInventoryRepository inventoryRepository;

    @Override
    @Transactional(readOnly = true)
    public InventoryDto getInventoryByMedicineId(Long medicineId) {
        MedicineInventory inv = inventoryRepository.findByMedicineId(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory for medicine id: " + medicineId));
        return mapToDto(inv);
    }

    @Override
    @Transactional
    public InventoryDto adjustStock(Long medicineId, StockAdjustmentRequest request) {
        MedicineInventory inv = inventoryRepository.findByMedicineId(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory for medicine id: " + medicineId));

        int newStock = Math.max(0, inv.getCurrentStock() + request.getChangeQuantity());
        inv.setCurrentStock(newStock);
        MedicineInventory saved = inventoryRepository.save(inv);

        log.info("Adjusted stock for medicine id {}: new stock = {}", medicineId, newStock);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowStockAlertDto> getLowStockAlerts(Long patientId) {
        return inventoryRepository.findLowStockInventories(patientId).stream()
                .map(inv -> LowStockAlertDto.builder()
                        .medicineId(inv.getMedicine().getId())
                        .medicineName(inv.getMedicine().getName())
                        .patientId(inv.getMedicine().getPatientId())
                        .currentStock(inv.getCurrentStock())
                        .minimumThreshold(inv.getMinimumThreshold())
                        .reorderQuantity(inv.getReorderQuantity())
                        .expiryDate(inv.getExpiryDate())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deductDoseFromStock(Long medicineId, int quantity) {
        inventoryRepository.findByMedicineId(medicineId).ifPresent(inv -> {
            int newStock = Math.max(0, inv.getCurrentStock() - quantity);
            inv.setCurrentStock(newStock);
            inventoryRepository.save(inv);
            log.info("Deducted {} dose from medicine {}. Remaining: {}", quantity, medicineId, newStock);
        });
    }

    private InventoryDto mapToDto(MedicineInventory inv) {
        return InventoryDto.builder()
                .id(inv.getId())
                .medicineId(inv.getMedicine().getId())
                .medicineName(inv.getMedicine().getName())
                .currentStock(inv.getCurrentStock())
                .minimumThreshold(inv.getMinimumThreshold())
                .reorderQuantity(inv.getReorderQuantity())
                .expiryDate(inv.getExpiryDate())
                .batchNumber(inv.getBatchNumber())
                .unitPrice(inv.getUnitPrice())
                .isLowStock(inv.isLowStock())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }
}
