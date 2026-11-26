package com.medicare.medicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PrescriptionRequest {
    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private Long doctorId;

    @NotBlank(message = "Doctor name is required")
    private String doctorName;

    @NotNull(message = "Prescription date is required")
    private LocalDate prescriptionDate;

    private String diagnosis;
    private String notes;
    private String prescriptionFilePath;
    private String fileName;
}
