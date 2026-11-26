package com.medicare.analytics.client;

import com.medicare.common.dto.ApiResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class HealthFeignClientFallback implements HealthFeignClient {

    @Override
    public ApiResponse<Map<String, Object>> getLatestVitals(Long patientId) {
        return ApiResponse.success(Collections.emptyMap());
    }

    @Override
    public ApiResponse<List<Map<String, Object>>> getTrends(Long patientId, String recordType, int days) {
        return ApiResponse.success(Collections.emptyList());
    }
}
