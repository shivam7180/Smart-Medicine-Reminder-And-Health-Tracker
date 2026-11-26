package com.medicare.health.service.impl;

import com.medicare.common.exception.ResourceNotFoundException;
import com.medicare.health.dto.HealthRecordRequest;
import com.medicare.health.dto.HealthRecordResponse;
import com.medicare.health.dto.LatestVitalsDto;
import com.medicare.health.entity.HealthRecord;
import com.medicare.health.repository.HealthRecordRepository;
import com.medicare.health.service.HealthRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthRecordServiceImpl implements HealthRecordService {

    private final HealthRecordRepository healthRecordRepository;

    @Override
    @Transactional
    public HealthRecordResponse addRecord(Long patientId, HealthRecordRequest request) {
        String normalizedType = request.getRecordType().toUpperCase().trim();
        LocalDateTime readingTime = request.getReadingTime() != null ? request.getReadingTime() : LocalDateTime.now();

        boolean abnormal = checkAbnormalFlag(normalizedType, request.getValuePrimary(), request.getValueSecondary());

        HealthRecord record = HealthRecord.builder()
                .patientId(patientId)
                .recordType(normalizedType)
                .valuePrimary(request.getValuePrimary())
                .valueSecondary(request.getValueSecondary())
                .unit(request.getUnit())
                .readingTime(readingTime)
                .notes(request.getNotes())
                .abnormalFlag(abnormal)
                .build();

        HealthRecord saved = healthRecordRepository.save(record);

        // Automatically trigger BMI recalculation if Weight or Height was logged
        if ("WEIGHT".equalsIgnoreCase(normalizedType) || "HEIGHT".equalsIgnoreCase(normalizedType)) {
            calculateAndSaveBmi(patientId, readingTime);
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public HealthRecordResponse updateRecord(Long patientId, Long recordId, HealthRecordRequest request) {
        HealthRecord record = healthRecordRepository.findByIdAndPatientId(recordId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthRecord", "id", recordId));

        String normalizedType = request.getRecordType().toUpperCase().trim();
        record.setRecordType(normalizedType);
        record.setValuePrimary(request.getValuePrimary());
        record.setValueSecondary(request.getValueSecondary());
        record.setUnit(request.getUnit());
        if (request.getReadingTime() != null) {
            record.setReadingTime(request.getReadingTime());
        }
        record.setNotes(request.getNotes());
        record.setAbnormalFlag(checkAbnormalFlag(normalizedType, request.getValuePrimary(), request.getValueSecondary()));

        HealthRecord updated = healthRecordRepository.save(record);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public HealthRecordResponse getRecordById(Long patientId, Long recordId) {
        HealthRecord record = healthRecordRepository.findByIdAndPatientId(recordId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthRecord", "id", recordId));
        return mapToResponse(record);
    }

    @Override
    @Transactional
    public void deleteRecord(Long patientId, Long recordId) {
        HealthRecord record = healthRecordRepository.findByIdAndPatientId(recordId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthRecord", "id", recordId));
        healthRecordRepository.delete(record);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HealthRecordResponse> getRecordHistory(Long patientId, String recordType, Pageable pageable) {
        if (recordType != null && !recordType.isBlank()) {
            return healthRecordRepository.findByPatientIdAndRecordTypeOrderByReadingTimeDesc(
                    patientId, recordType.toUpperCase().trim(), pageable).map(this::mapToResponse);
        }
        return healthRecordRepository.findByPatientIdOrderByReadingTimeDesc(patientId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthRecordResponse> getTrendData(Long patientId, String recordType, int days) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days);
        return healthRecordRepository.findByPatientIdAndRecordTypeAndReadingTimeBetweenOrderByReadingTimeAsc(
                patientId, recordType.toUpperCase().trim(), start, end)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LatestVitalsDto getLatestVitals(Long patientId) {
        List<HealthRecord> latestList = healthRecordRepository.findLatestRecordsForEachType(patientId);

        LatestVitalsDto.LatestVitalsDtoBuilder builder = LatestVitalsDto.builder();

        for (HealthRecord r : latestList) {
            switch (r.getRecordType().toUpperCase()) {
                case "BLOOD_PRESSURE":
                    builder.bloodPressureSystolic(r.getValuePrimary());
                    builder.bloodPressureDiastolic(r.getValueSecondary());
                    break;
                case "BLOOD_SUGAR":
                    builder.bloodSugar(r.getValuePrimary());
                    break;
                case "HEART_RATE":
                    builder.heartRate(r.getValuePrimary());
                    break;
                case "TEMPERATURE":
                    builder.temperature(r.getValuePrimary());
                    break;
                case "WEIGHT":
                    builder.weight(r.getValuePrimary());
                    break;
                case "HEIGHT":
                    builder.height(r.getValuePrimary());
                    break;
                case "BMI":
                    builder.bmi(r.getValuePrimary());
                    builder.bmiCategory(classifyBmi(r.getValuePrimary()));
                    break;
                case "SPO2":
                    builder.spo2(r.getValuePrimary());
                    break;
                case "WATER_INTAKE":
                    builder.waterIntake(r.getValuePrimary());
                    break;
                case "SLEEP":
                    builder.sleepHours(r.getValuePrimary());
                    break;
            }
        }

        return builder.build();
    }

    private void calculateAndSaveBmi(Long patientId, LocalDateTime readingTime) {
        Optional<HealthRecord> weightOpt = healthRecordRepository.findFirstByPatientIdAndRecordTypeOrderByReadingTimeDesc(patientId, "WEIGHT");
        Optional<HealthRecord> heightOpt = healthRecordRepository.findFirstByPatientIdAndRecordTypeOrderByReadingTimeDesc(patientId, "HEIGHT");

        if (weightOpt.isPresent() && heightOpt.isPresent()) {
            double weightKg = weightOpt.get().getValuePrimary().doubleValue();
            double heightCm = heightOpt.get().getValuePrimary().doubleValue();

            if (heightCm > 0) {
                double heightM = heightCm / 100.0;
                double bmiVal = weightKg / (heightM * heightM);
                BigDecimal bmiRounded = BigDecimal.valueOf(bmiVal).setScale(1, RoundingMode.HALF_UP);

                HealthRecord bmiRecord = HealthRecord.builder()
                        .patientId(patientId)
                        .recordType("BMI")
                        .valuePrimary(bmiRounded)
                        .unit("kg/m²")
                        .readingTime(readingTime)
                        .notes("Auto-calculated from Weight (" + weightKg + "kg) and Height (" + heightCm + "cm)")
                        .abnormalFlag(bmiVal < 18.5 || bmiVal >= 25.0)
                        .build();

                healthRecordRepository.save(bmiRecord);
                log.info("Auto-calculated BMI {} for patient {}", bmiRounded, patientId);
            }
        }
    }

    private boolean checkAbnormalFlag(String type, BigDecimal primary, BigDecimal secondary) {
        if (primary == null) return false;
        double val = primary.doubleValue();

        switch (type) {
            case "BLOOD_PRESSURE":
                double sys = val;
                double dia = secondary != null ? secondary.doubleValue() : 80;
                return (sys > 140 || sys < 90 || dia > 90 || dia < 60);
            case "BLOOD_SUGAR":
                return (val > 140 || val < 70);
            case "HEART_RATE":
                return (val > 100 || val < 60);
            case "SPO2":
                return (val < 95);
            case "TEMPERATURE":
                return (val > 38.0 || val < 35.5);
            default:
                return false;
        }
    }

    private String classifyBmi(BigDecimal bmi) {
        if (bmi == null) return "N/A";
        double val = bmi.doubleValue();
        if (val < 18.5) return "UNDERWEIGHT";
        if (val < 25.0) return "NORMAL";
        if (val < 30.0) return "OVERWEIGHT";
        return "OBESE";
    }

    private HealthRecordResponse mapToResponse(HealthRecord r) {
        String warning = Boolean.TRUE.equals(r.getAbnormalFlag())
                ? "Reading falls outside standard normal reference boundaries. Monitor closely."
                : null;

        return HealthRecordResponse.builder()
                .id(r.getId())
                .patientId(r.getPatientId())
                .recordType(r.getRecordType())
                .valuePrimary(r.getValuePrimary())
                .valueSecondary(r.getValueSecondary())
                .unit(r.getUnit())
                .readingTime(r.getReadingTime())
                .notes(r.getNotes())
                .abnormalFlag(r.getAbnormalFlag())
                .warningMessage(warning)
                .createdAt(r.getCreatedAt())
                .build();
    }
}
