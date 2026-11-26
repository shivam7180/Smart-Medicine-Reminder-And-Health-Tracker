package com.medicare.medicine.service;

import com.medicare.medicine.dto.PrescriptionRequest;
import com.medicare.medicine.dto.PrescriptionResponse;

import java.util.List;

public interface PrescriptionService {
    PrescriptionResponse createPrescription(PrescriptionRequest request);
    List<PrescriptionResponse> getPrescriptionsByPatient(Long patientId);
    PrescriptionResponse getPrescriptionById(Long id);
    void deletePrescription(Long id);
}
