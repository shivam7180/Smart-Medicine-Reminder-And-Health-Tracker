package com.medicare.medicine.service.impl;

import com.medicare.common.exception.ResourceNotFoundException;
import com.medicare.medicine.dto.PrescriptionRequest;
import com.medicare.medicine.dto.PrescriptionResponse;
import com.medicare.medicine.entity.Prescription;
import com.medicare.medicine.repository.PrescriptionRepository;
import com.medicare.medicine.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    @Override
    @Transactional
    public PrescriptionResponse createPrescription(PrescriptionRequest request) {
        Prescription prescription = Prescription.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .doctorName(request.getDoctorName().trim())
                .prescriptionDate(request.getPrescriptionDate())
                .diagnosis(request.getDiagnosis())
                .notes(request.getNotes())
                .prescriptionFilePath(request.getPrescriptionFilePath())
                .fileName(request.getFileName())
                .build();

        return mapToResponse(prescriptionRepository.save(prescription));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPrescriptionsByPatient(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));
        return mapToResponse(prescription);
    }

    @Override
    @Transactional
    public void deletePrescription(Long id) {
        if (!prescriptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Prescription", "id", id);
        }
        prescriptionRepository.deleteById(id);
    }

    private PrescriptionResponse mapToResponse(Prescription p) {
        return PrescriptionResponse.builder()
                .id(p.getId())
                .patientId(p.getPatientId())
                .doctorId(p.getDoctorId())
                .doctorName(p.getDoctorName())
                .prescriptionDate(p.getPrescriptionDate())
                .diagnosis(p.getDiagnosis())
                .notes(p.getNotes())
                .prescriptionFilePath(p.getPrescriptionFilePath())
                .fileName(p.getFileName())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
