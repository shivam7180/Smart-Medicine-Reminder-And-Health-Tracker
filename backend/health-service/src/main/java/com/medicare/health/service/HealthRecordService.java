package com.medicare.health.service;

import com.medicare.health.dto.HealthRecordRequest;
import com.medicare.health.dto.HealthRecordResponse;
import com.medicare.health.dto.LatestVitalsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HealthRecordService {
    HealthRecordResponse addRecord(Long patientId, HealthRecordRequest request);
    HealthRecordResponse updateRecord(Long patientId, Long recordId, HealthRecordRequest request);
    HealthRecordResponse getRecordById(Long patientId, Long recordId);
    void deleteRecord(Long patientId, Long recordId);
    Page<HealthRecordResponse> getRecordHistory(Long patientId, String recordType, Pageable pageable);
    List<HealthRecordResponse> getTrendData(Long patientId, String recordType, int days);
    LatestVitalsDto getLatestVitals(Long patientId);
}
