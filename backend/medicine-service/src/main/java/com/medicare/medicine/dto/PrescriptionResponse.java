package com.medicare.medicine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponse {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private String doctorName;
    private LocalDate prescriptionDate;
    private String diagnosis;
    private String notes;
    private String prescriptionFilePath;
    private String fileName;
    private LocalDateTime createdAt;
}
