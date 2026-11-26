package com.medicare.medicine.service;

import com.medicare.medicine.dto.MedicineRequest;
import com.medicare.medicine.dto.MedicineResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MedicineService {
    MedicineResponse addMedicine(Long patientId, MedicineRequest request);
    MedicineResponse updateMedicine(Long patientId, Long medicineId, MedicineRequest request);
    MedicineResponse getMedicineById(Long patientId, Long medicineId);
    void deleteMedicine(Long patientId, Long medicineId);
    List<MedicineResponse> getActiveMedicines(Long patientId);
    Page<MedicineResponse> searchMedicines(Long patientId, String keyword, Long categoryId, Boolean isActive, Pageable pageable);
    long countActiveMedicines(Long patientId);
}
