package com.medicare.appointment.controller;

import com.medicare.appointment.dto.SpecializationDto;
import com.medicare.appointment.service.DoctorService;
import com.medicare.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/specializations")
@RequiredArgsConstructor
@Tag(name = "Doctor Specializations", description = "Medical specializations (Cardiology, Endocrinology, etc.)")
public class SpecializationController {

    private final DoctorService doctorService;

    @GetMapping
    @Operation(summary = "List all medical specializations")
    public ResponseEntity<ApiResponse<List<SpecializationDto>>> getAllSpecializations() {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getAllSpecializations()));
    }
}
