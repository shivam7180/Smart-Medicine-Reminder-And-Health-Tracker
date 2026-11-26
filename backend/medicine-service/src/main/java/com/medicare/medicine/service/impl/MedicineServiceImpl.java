package com.medicare.medicine.service.impl;

import com.medicare.common.exception.ResourceNotFoundException;
import com.medicare.medicine.dto.MedicineRequest;
import com.medicare.medicine.dto.MedicineResponse;
import com.medicare.medicine.entity.Medicine;
import com.medicare.medicine.entity.MedicineCategory;
import com.medicare.medicine.entity.MedicineInventory;
import com.medicare.medicine.repository.MedicineCategoryRepository;
import com.medicare.medicine.repository.MedicineInventoryRepository;
import com.medicare.medicine.repository.MedicineRepository;
import com.medicare.medicine.service.MedicineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineCategoryRepository categoryRepository;
    private final MedicineInventoryRepository inventoryRepository;

    @Override
    @Transactional
    public MedicineResponse addMedicine(Long patientId, MedicineRequest request) {
        MedicineCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElse(null);
        }

        Medicine medicine = Medicine.builder()
                .patientId(patientId)
                .category(category)
                .name(request.getName().trim())
                .genericName(request.getGenericName())
                .medicineType(request.getMedicineType())
                .strength(request.getStrength())
                .dosageInstructions(request.getDosageInstructions())
                .beforeAfterFood(request.getBeforeAfterFood() != null ? request.getBeforeAfterFood() : "AFTER_FOOD")
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .prescribingDoctor(request.getPrescribingDoctor())
                .notes(request.getNotes())
                .isActive(true)
                .build();

        Medicine savedMedicine = medicineRepository.save(medicine);

        // Initialize inventory
        MedicineInventory inventory = MedicineInventory.builder()
                .medicine(savedMedicine)
                .currentStock(request.getInitialStock() != null ? request.getInitialStock() : 30)
                .minimumThreshold(request.getMinimumThreshold() != null ? request.getMinimumThreshold() : 5)
                .reorderQuantity(request.getReorderQuantity() != null ? request.getReorderQuantity() : 30)
                .expiryDate(request.getExpiryDate())
                .batchNumber(request.getBatchNumber())
                .unitPrice(request.getUnitPrice())
                .build();

        MedicineInventory savedInventory = inventoryRepository.save(inventory);
        savedMedicine.setInventory(savedInventory);

        log.info("Saved new medicine {} for patient {}", savedMedicine.getName(), patientId);
        return mapToResponse(savedMedicine);
    }

    @Override
    @Transactional
    public MedicineResponse updateMedicine(Long patientId, Long medicineId, MedicineRequest request) {
        Medicine medicine = medicineRepository.findByIdAndPatientId(medicineId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", medicineId));

        if (request.getCategoryId() != null) {
            MedicineCategory category = categoryRepository.findById(request.getCategoryId()).orElse(null);
            medicine.setCategory(category);
        }

        medicine.setName(request.getName().trim());
        medicine.setGenericName(request.getGenericName());
        medicine.setMedicineType(request.getMedicineType());
        medicine.setStrength(request.getStrength());
        medicine.setDosageInstructions(request.getDosageInstructions());
        medicine.setBeforeAfterFood(request.getBeforeAfterFood());
        medicine.setStartDate(request.getStartDate());
        medicine.setEndDate(request.getEndDate());
        medicine.setPrescribingDoctor(request.getPrescribingDoctor());
        medicine.setNotes(request.getNotes());

        // Update inventory fields if provided
        if (medicine.getInventory() != null) {
            MedicineInventory inv = medicine.getInventory();
            if (request.getMinimumThreshold() != null) inv.setMinimumThreshold(request.getMinimumThreshold());
            if (request.getReorderQuantity() != null) inv.setReorderQuantity(request.getReorderQuantity());
            if (request.getExpiryDate() != null) inv.setExpiryDate(request.getExpiryDate());
            if (request.getBatchNumber() != null) inv.setBatchNumber(request.getBatchNumber());
            if (request.getUnitPrice() != null) inv.setUnitPrice(request.getUnitPrice());
            inventoryRepository.save(inv);
        }

        Medicine updated = medicineRepository.save(medicine);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineResponse getMedicineById(Long patientId, Long medicineId) {
        Medicine medicine = medicineRepository.findByIdAndPatientId(medicineId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", medicineId));
        return mapToResponse(medicine);
    }

    @Override
    @Transactional
    public void deleteMedicine(Long patientId, Long medicineId) {
        Medicine medicine = medicineRepository.findByIdAndPatientId(medicineId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", medicineId));
        medicine.setIsActive(false); // Soft delete
        medicineRepository.save(medicine);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponse> getActiveMedicines(Long patientId) {
        return medicineRepository.findByPatientIdAndIsActiveTrue(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicineResponse> searchMedicines(Long patientId, String keyword, Long categoryId, Boolean isActive, Pageable pageable) {
        return medicineRepository.searchMedicines(patientId, keyword, categoryId, isActive, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveMedicines(Long patientId) {
        return medicineRepository.countByPatientIdAndIsActiveTrue(patientId);
    }

    private MedicineResponse mapToResponse(Medicine m) {
        MedicineInventory inv = m.getInventory();
        return MedicineResponse.builder()
                .id(m.getId())
                .patientId(m.getPatientId())
                .categoryId(m.getCategory() != null ? m.getCategory().getId() : null)
                .categoryName(m.getCategory() != null ? m.getCategory().getName() : null)
                .name(m.getName())
                .genericName(m.getGenericName())
                .medicineType(m.getMedicineType())
                .strength(m.getStrength())
                .dosageInstructions(m.getDosageInstructions())
                .beforeAfterFood(m.getBeforeAfterFood())
                .startDate(m.getStartDate())
                .endDate(m.getEndDate())
                .prescribingDoctor(m.getPrescribingDoctor())
                .notes(m.getNotes())
                .isActive(m.getIsActive())
                .currentStock(inv != null ? inv.getCurrentStock() : 0)
                .minimumThreshold(inv != null ? inv.getMinimumThreshold() : 0)
                .reorderQuantity(inv != null ? inv.getReorderQuantity() : 0)
                .expiryDate(inv != null ? inv.getExpiryDate() : null)
                .batchNumber(inv != null ? inv.getBatchNumber() : null)
                .unitPrice(inv != null ? inv.getUnitPrice() : null)
                .isLowStock(inv != null && inv.isLowStock())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
