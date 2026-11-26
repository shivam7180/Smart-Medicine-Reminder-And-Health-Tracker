package com.medicare.medicine.dto;

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
public class InventoryDto {
    private Long id;
    private Long medicineId;
    private String medicineName;
    private Integer currentStock;
    private Integer minimumThreshold;
    private Integer reorderQuantity;
    private LocalDate expiryDate;
    private String batchNumber;
    private BigDecimal unitPrice;
    private Boolean isLowStock;
    private LocalDateTime updatedAt;
}
