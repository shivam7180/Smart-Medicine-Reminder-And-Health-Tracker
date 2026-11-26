package com.medicare.appointment.service;

import com.medicare.appointment.dto.AppointmentResponse;
import com.medicare.appointment.dto.BookAppointmentRequest;
import com.medicare.common.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentService {
    AppointmentResponse bookAppointment(Long patientId, BookAppointmentRequest request);
    AppointmentResponse rescheduleAppointment(Long patientId, Long appointmentId, LocalDate newDate, LocalTime newTime);
    AppointmentResponse updateStatus(Long appointmentId, AppointmentStatus status, String doctorNotes);
    List<AppointmentResponse> getAppointmentsByPatient(Long patientId);
    List<AppointmentResponse> getUpcomingAppointmentsByPatient(Long patientId);
    List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId);
    AppointmentResponse getAppointmentById(Long id);
}
