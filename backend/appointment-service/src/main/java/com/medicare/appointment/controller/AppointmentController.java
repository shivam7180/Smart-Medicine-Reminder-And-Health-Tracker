package com.medicare.appointment.controller;

import com.medicare.appointment.dto.AppointmentResponse;
import com.medicare.appointment.dto.BookAppointmentRequest;
import com.medicare.appointment.service.AppointmentService;
import com.medicare.common.dto.ApiResponse;
import com.medicare.common.enums.AppointmentStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Appointment bookings, schedule status, and patient-doctor consultations")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @Operation(summary = "Book a new doctor appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> bookAppointment(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId,
            @Valid @RequestBody BookAppointmentRequest request) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        if (patientId == null) patientId = 1L;
        return new ResponseEntity<>(ApiResponse.success(appointmentService.bookAppointment(patientId, request), "Appointment booked successfully"), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/reschedule")
    @Operation(summary = "Reschedule an existing appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> reschedule(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime newTime,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(appointmentService.rescheduleAppointment(patientId, id, newDate, newTime), "Appointment rescheduled"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update appointment status (CONFIRMED, COMPLETED, CANCELLED)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status,
            @RequestParam(required = false) String doctorNotes) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.updateStatus(id, status, doctorNotes), "Status updated"));
    }

    @GetMapping("/patient")
    @Operation(summary = "Get all appointments for patient")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getPatientAppointments(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointmentsByPatient(patientId)));
    }

    @GetMapping("/patient/upcoming")
    @Operation(summary = "Get upcoming appointments for patient")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getUpcomingPatientAppointments(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "patientId", required = false) Long paramPatientId) {
        Long patientId = paramPatientId != null ? paramPatientId : headerUserId;
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getUpcomingAppointmentsByPatient(patientId)));
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get all appointments assigned to a doctor")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getDoctorAppointments(@PathVariable Long doctorId) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointmentsByDoctor(doctorId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get appointment details by ID")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointmentById(id)));
    }
}
