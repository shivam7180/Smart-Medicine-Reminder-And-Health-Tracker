package com.medicare.medicine.dto;

import com.medicare.common.enums.MedicineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineResponse {
    private Long id;
    private Long patientId;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String genericName;
    private MedicineType medicineType;
    private String strength;
    private String dosageInstructions;
    private String beforeAfterFood;
    private LocalDate startDate;
    private LocalDate endDate;
    private String prescribingDoctor;
    private String notes;
    private Boolean isActive;

    // Inventory details embedded
    private Integer currentStock;
    private Integer minimumThreshold;
    private Integer reorderQuantity;
    private LocalDate expiryDate;
    private String batchNumber;
    private BigDecimal unitPrice;
    private Boolean isLowStock;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
