package com.medicare.appointment.controller;

import com.medicare.appointment.dto.DoctorDto;
import com.medicare.appointment.service.DoctorService;
import com.medicare.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Doctor directory, specialties, credentials, and consultation fees")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @Operation(summary = "List all active registered doctors")
    public ResponseEntity<ApiResponse<List<DoctorDto>>> getAllDoctors() {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getAllActiveDoctors()));
    }

    @GetMapping("/search")
    @Operation(summary = "Search doctors by specialization and keyword")
    public ResponseEntity<ApiResponse<Page<DoctorDto>>> searchDoctors(
            @RequestParam(required = false) Long specializationId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(doctorService.searchDoctors(specializationId, keyword, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor details by ID")
    public ResponseEntity<ApiResponse<DoctorDto>> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getDoctorById(id)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get doctor profile by User ID")
    public ResponseEntity<ApiResponse<DoctorDto>> getDoctorByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getDoctorByUserId(userId)));
    }

    @PostMapping("/profile")
    @Operation(summary = "Create or update doctor profile")
    public ResponseEntity<ApiResponse<DoctorDto>> updateDoctorProfile(@RequestBody DoctorDto dto) {
        return ResponseEntity.ok(ApiResponse.success(doctorService.createOrUpdateDoctorProfile(dto), "Doctor profile updated"));
    }
}
