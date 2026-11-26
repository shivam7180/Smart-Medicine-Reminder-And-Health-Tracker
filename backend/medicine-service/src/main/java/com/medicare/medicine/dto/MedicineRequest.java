package com.medicare.medicine.dto;

import com.medicare.common.enums.MedicineType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MedicineRequest {
    @NotBlank(message = "Medicine name is required")
    private String name;

    private String genericName;
    private Long categoryId;

    @NotNull(message = "Medicine type is required (TABLET, CAPSULE, SYRUP, etc.)")
    private MedicineType medicineType;

    @NotBlank(message = "Strength/dosage is required (e.g. 500 mg)")
    private String strength;

    private String dosageInstructions;
    private String beforeAfterFood; // BEFORE_FOOD, AFTER_FOOD, WITH_FOOD, ANYTIME

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;
    private String prescribingDoctor;
    private String notes;

    // Initial Inventory Configuration
    private Integer initialStock = 30;
    private Integer minimumThreshold = 5;
    private Integer reorderQuantity = 30;
    private LocalDate expiryDate;
    private String batchNumber;
    private BigDecimal unitPrice = BigDecimal.ZERO;
}
