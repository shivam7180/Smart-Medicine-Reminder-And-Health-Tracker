package com.medicare.analytics.client;

import com.medicare.common.dto.ApiResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class ReminderFeignClientFallback implements ReminderFeignClient {

    @Override
    public ApiResponse<Map<String, Object>> getAdherence(Long patientId, int days) {
        return ApiResponse.success(Collections.emptyMap());
    }

    @Override
    public ApiResponse<Map<String, Object>> getTodaySummary(Long patientId) {
        return ApiResponse.success(Collections.emptyMap());
    }
}
