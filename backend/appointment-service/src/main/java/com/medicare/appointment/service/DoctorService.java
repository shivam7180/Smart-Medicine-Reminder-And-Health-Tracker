package com.medicare.appointment.service;

import com.medicare.appointment.dto.DoctorDto;
import com.medicare.appointment.dto.SpecializationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DoctorService {
    List<DoctorDto> getAllActiveDoctors();
    Page<DoctorDto> searchDoctors(Long specializationId, String keyword, Pageable pageable);
    DoctorDto getDoctorById(Long id);
    DoctorDto getDoctorByUserId(Long userId);
    DoctorDto createOrUpdateDoctorProfile(DoctorDto dto);
    List<SpecializationDto> getAllSpecializations();
}
