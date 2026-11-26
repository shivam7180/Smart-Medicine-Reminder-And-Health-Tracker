package com.medicare.reminder.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "medicine-service", fallback = MedicineInventoryClientFallback.class)
public interface MedicineInventoryClient {

    @PostMapping("/api/v1/inventory/internal/deduct")
    void deductStock(
            @RequestParam("medicineId") Long medicineId,
            @RequestParam("quantity") int quantity);
}
