package com.medicare.appointment.service.impl;

import com.medicare.appointment.dto.AppointmentResponse;
import com.medicare.appointment.dto.BookAppointmentRequest;
import com.medicare.appointment.entity.Appointment;
import com.medicare.appointment.entity.Doctor;
import com.medicare.appointment.repository.AppointmentRepository;
import com.medicare.appointment.repository.DoctorRepository;
import com.medicare.appointment.service.AppointmentService;
import com.medicare.common.enums.AppointmentStatus;
import com.medicare.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional
    public AppointmentResponse bookAppointment(Long patientId, BookAppointmentRequest request) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", request.getDoctorId()));

        Appointment appt = Appointment.builder()
                .patientId(patientId)
                .doctor(doctor)
                .appointmentDate(request.getAppointmentDate())
                .appointmentTime(request.getAppointmentTime())
                .reason(request.getReason())
                .status(AppointmentStatus.PENDING)
                .build();

        Appointment saved = appointmentRepository.save(appt);
        log.info("Booked appointment {} for patient {} with doctor {}", saved.getId(), patientId, doctor.getFullName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentResponse rescheduleAppointment(Long patientId, Long appointmentId, LocalDate newDate, LocalTime newTime) {
        Appointment appt = appointmentRepository.findByIdAndPatientId(appointmentId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));

        appt.setAppointmentDate(newDate);
        appt.setAppointmentTime(newTime);
        appt.setStatus(AppointmentStatus.RESCHEDULED);

        return mapToResponse(appointmentRepository.save(appt));
    }

    @Override
    @Transactional
    public AppointmentResponse updateStatus(Long appointmentId, AppointmentStatus status, String doctorNotes) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));

        appt.setStatus(status);
        if (doctorNotes != null) {
            appt.setDoctorNotes(doctorNotes);
        }

        return mapToResponse(appointmentRepository.save(appt));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(patientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUpcomingAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientIdAndAppointmentDateGreaterThanEqualOrderByAppointmentDateAsc(
                patientId, LocalDate.now())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(doctorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        return mapToResponse(appt);
    }

    private AppointmentResponse mapToResponse(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .patientId(a.getPatientId())
                .doctorId(a.getDoctor().getId())
                .doctorName(a.getDoctor().getFullName())
                .specialization(a.getDoctor().getSpecialization() != null ? a.getDoctor().getSpecialization().getName() : "General Medicine")
                .appointmentDate(a.getAppointmentDate())
                .appointmentTime(a.getAppointmentTime())
                .reason(a.getReason())
                .status(a.getStatus())
                .doctorNotes(a.getDoctorNotes())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
