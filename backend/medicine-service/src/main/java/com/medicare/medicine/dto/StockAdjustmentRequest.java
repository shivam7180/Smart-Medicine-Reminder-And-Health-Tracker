package com.medicare.medicine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustmentRequest {
    @NotNull(message = "Quantity change is required (positive to restock, negative to deduct)")
    private Integer changeQuantity;

    private String reason; // e.g., "Refill purchase", "Manual correction", "Dose taken"
}
