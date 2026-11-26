package com.medicare.health.service;

import com.medicare.health.dto.HealthRecordRequest;
import com.medicare.health.dto.HealthRecordResponse;
import com.medicare.health.entity.HealthRecord;
import com.medicare.health.repository.HealthRecordRepository;
import com.medicare.health.service.impl.HealthRecordServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthRecordServiceTest {

    @Mock
    private HealthRecordRepository healthRecordRepository;

    @InjectMocks
    private HealthRecordServiceImpl healthRecordService;

    @Test
    @DisplayName("Should detect high blood pressure and automatically flag as abnormal")
    void testAbnormalBloodPressureFlag() {
        HealthRecordRequest request = new HealthRecordRequest();
        request.setRecordType("BLOOD_PRESSURE");
        request.setValuePrimary(new BigDecimal("155.0")); // Systolic > 140
        request.setValueSecondary(new BigDecimal("98.0")); // Diastolic > 90
        request.setUnit("mmHg");

        when(healthRecordRepository.save(any(HealthRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HealthRecordResponse response = healthRecordService.addRecord(3L, request);

        assertNotNull(response);
        assertTrue(response.getAbnormalFlag());
        assertNotNull(response.getWarningMessage());
    }

    @Test
    @DisplayName("Should flag normal blood pressure readings as non-abnormal")
    void testNormalBloodPressureFlag() {
        HealthRecordRequest request = new HealthRecordRequest();
        request.setRecordType("BLOOD_PRESSURE");
        request.setValuePrimary(new BigDecimal("120.0"));
        request.setValueSecondary(new BigDecimal("80.0"));
        request.setUnit("mmHg");

        when(healthRecordRepository.save(any(HealthRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HealthRecordResponse response = healthRecordService.addRecord(3L, request);

        assertNotNull(response);
        assertFalse(response.getAbnormalFlag());
        assertNull(response.getWarningMessage());
    }
}
