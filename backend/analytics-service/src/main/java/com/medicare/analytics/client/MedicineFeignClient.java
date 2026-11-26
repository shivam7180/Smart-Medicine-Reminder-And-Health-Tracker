package com.medicare.analytics.client;

import com.medicare.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "medicine-service", fallback = MedicineFeignClientFallback.class)
public interface MedicineFeignClient {

    @GetMapping("/api/v1/medicines/count")
    ApiResponse<Long> countActive(@RequestParam("patientId") Long patientId);

    @GetMapping("/api/v1/medicines/active")
    ApiResponse<List<Map<String, Object>>> getActiveMedicines(@RequestParam("patientId") Long patientId);
}
