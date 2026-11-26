package com.medicare.appointment.service.impl;

import com.medicare.appointment.dto.DoctorDto;
import com.medicare.appointment.dto.SpecializationDto;
import com.medicare.appointment.entity.Doctor;
import com.medicare.appointment.entity.DoctorSpecialization;
import com.medicare.appointment.repository.DoctorRepository;
import com.medicare.appointment.repository.DoctorSpecializationRepository;
import com.medicare.appointment.service.DoctorService;
import com.medicare.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorSpecializationRepository specializationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDto> getAllActiveDoctors() {
        return doctorRepository.findByIsActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorDto> searchDoctors(Long specializationId, String keyword, Pageable pageable) {
        return doctorRepository.searchDoctors(specializationId, keyword, pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorDto getDoctorById(Long id) {
        Doctor doc = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
        return mapToDto(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorDto getDoctorByUserId(Long userId) {
        Doctor doc = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "userId", userId));
        return mapToDto(doc);
    }

    @Override
    @Transactional
    public DoctorDto createOrUpdateDoctorProfile(DoctorDto dto) {
        DoctorSpecialization spec = null;
        if (dto.getSpecializationId() != null) {
            spec = specializationRepository.findById(dto.getSpecializationId()).orElse(null);
        }

        Doctor doc = doctorRepository.findByUserId(dto.getUserId())
                .orElseGet(() -> Doctor.builder().userId(dto.getUserId()).build());

        doc.setFullName(dto.getFullName());
        doc.setEmail(dto.getEmail());
        doc.setPhoneNumber(dto.getPhoneNumber());
        doc.setSpecialization(spec);
        doc.setQualification(dto.getQualification());
        doc.setExperienceYears(dto.getExperienceYears() != null ? dto.getExperienceYears() : 1);
        doc.setConsultationFee(dto.getConsultationFee());
        doc.setBio(dto.getBio());
        doc.setAvailableDays(dto.getAvailableDays());
        doc.setIsActive(true);

        return mapToDto(doctorRepository.save(doc));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecializationDto> getAllSpecializations() {
        return specializationRepository.findAll().stream()
                .map(s -> SpecializationDto.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .description(s.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    private DoctorDto mapToDto(Doctor d) {
        return DoctorDto.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .fullName(d.getFullName())
                .email(d.getEmail())
                .phoneNumber(d.getPhoneNumber())
                .specializationId(d.getSpecialization() != null ? d.getSpecialization().getId() : null)
                .specializationName(d.getSpecialization() != null ? d.getSpecialization().getName() : "General Medicine")
                .qualification(d.getQualification())
                .experienceYears(d.getExperienceYears())
                .consultationFee(d.getConsultationFee())
                .bio(d.getBio())
                .availableDays(d.getAvailableDays())
                .isActive(d.getIsActive())
                .build();
    }
}
