package com.medicare.analytics.client;

import com.medicare.common.dto.ApiResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class AppointmentFeignClientFallback implements AppointmentFeignClient {

    @Override
    public ApiResponse<List<Map<String, Object>>> getUpcomingAppointments(Long patientId) {
        return ApiResponse.success(Collections.emptyList());
    }
}
