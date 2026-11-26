package com.medicare.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDto {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Long specializationId;
    private String specializationName;
    private String qualification;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private String bio;
    private String availableDays;
    private Boolean isActive;
}
