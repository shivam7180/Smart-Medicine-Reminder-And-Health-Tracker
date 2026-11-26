package com.medicare.analytics.client;

import com.medicare.common.dto.ApiResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class MedicineFeignClientFallback implements MedicineFeignClient {

    @Override
    public ApiResponse<Long> countActive(Long patientId) {
        return ApiResponse.success(0L);
    }

    @Override
    public ApiResponse<List<Map<String, Object>>> getActiveMedicines(Long patientId) {
        return ApiResponse.success(Collections.emptyList());
    }
}
