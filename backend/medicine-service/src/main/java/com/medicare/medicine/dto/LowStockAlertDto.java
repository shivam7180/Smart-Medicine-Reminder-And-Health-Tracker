package com.medicare.medicine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockAlertDto {
    private Long medicineId;
    private String medicineName;
    private Long patientId;
    private Integer currentStock;
    private Integer minimumThreshold;
    private Integer reorderQuantity;
    private LocalDate expiryDate;
}
