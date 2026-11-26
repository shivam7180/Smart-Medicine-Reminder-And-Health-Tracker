package com.medicare.reminder.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MedicineInventoryClientFallback implements MedicineInventoryClient {

    @Override
    public void deductStock(Long medicineId, int quantity) {
        log.warn("Fallback triggered: Unable to deduct inventory for medicine id {} - medicine-service unavailable", medicineId);
    }
}
