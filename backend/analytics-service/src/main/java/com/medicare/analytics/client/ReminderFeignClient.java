package com.medicare.analytics.client;

import com.medicare.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "reminder-service", fallback = ReminderFeignClientFallback.class)
public interface ReminderFeignClient {

    @GetMapping("/api/v1/doses/adherence")
    ApiResponse<Map<String, Object>> getAdherence(
            @RequestParam("patientId") Long patientId,
            @RequestParam("days") int days);

    @GetMapping("/api/v1/doses/today/summary")
    ApiResponse<Map<String, Object>> getTodaySummary(@RequestParam("patientId") Long patientId);
}
