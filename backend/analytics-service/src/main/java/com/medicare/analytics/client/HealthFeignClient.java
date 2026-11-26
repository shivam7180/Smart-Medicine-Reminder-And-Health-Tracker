package com.medicare.analytics.client;

import com.medicare.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "health-service", fallback = HealthFeignClientFallback.class)
public interface HealthFeignClient {

    @GetMapping("/api/v1/health/vitals/latest")
    ApiResponse<Map<String, Object>> getLatestVitals(@RequestParam("patientId") Long patientId);

    @GetMapping("/api/v1/health/trends")
    ApiResponse<List<Map<String, Object>>> getTrends(
            @RequestParam("patientId") Long patientId,
            @RequestParam("recordType") String recordType,
            @RequestParam("days") int days);
}
