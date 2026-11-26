package com.medicare.medicine.service;

import com.medicare.common.enums.MedicineType;
import com.medicare.medicine.dto.MedicineRequest;
import com.medicare.medicine.dto.MedicineResponse;
import com.medicare.medicine.entity.Medicine;
import com.medicare.medicine.entity.MedicineInventory;
import com.medicare.medicine.repository.MedicineCategoryRepository;
import com.medicare.medicine.repository.MedicineInventoryRepository;
import com.medicare.medicine.repository.MedicineRepository;
import com.medicare.medicine.service.impl.MedicineServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private MedicineCategoryRepository categoryRepository;

    @Mock
    private MedicineInventoryRepository inventoryRepository;

    @InjectMocks
    private MedicineServiceImpl medicineService;

    @Test
    @DisplayName("Should successfully save new medicine and initialize inventory")
    void testAddMedicineSuccess() {
        MedicineRequest request = new MedicineRequest();
        request.setName("Metformin");
        request.setMedicineType(MedicineType.TABLET);
        request.setStrength("500 mg");
        request.setStartDate(LocalDate.now());
        request.setInitialStock(40);
        request.setMinimumThreshold(10);

        when(medicineRepository.save(any(Medicine.class))).thenAnswer(invocation -> {
            Medicine m = invocation.getArgument(0);
            m.setId(1L);
            return m;
        });

        when(inventoryRepository.save(any(MedicineInventory.class))).thenAnswer(invocation -> {
            MedicineInventory inv = invocation.getArgument(0);
            inv.setId(1L);
            return inv;
        });

        MedicineResponse response = medicineService.addMedicine(3L, request);

        assertNotNull(response);
        assertEquals("Metformin", response.getName());
        assertEquals(40, response.getCurrentStock());
        assertFalse(response.getIsLowStock());
        verify(medicineRepository, times(1)).save(any());
        verify(inventoryRepository, times(1)).save(any());
    }
}
