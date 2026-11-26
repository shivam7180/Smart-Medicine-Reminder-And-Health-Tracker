package com.medicare.analytics.client;

import com.medicare.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "appointment-service", fallback = AppointmentFeignClientFallback.class)
public interface AppointmentFeignClient {

    @GetMapping("/api/v1/appointments/patient/upcoming")
    ApiResponse<List<Map<String, Object>>> getUpcomingAppointments(@RequestParam("patientId") Long patientId);
}
