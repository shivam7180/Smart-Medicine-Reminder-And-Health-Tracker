package com.medicare.reminder.service;

import com.medicare.common.enums.DoseStatus;
import com.medicare.reminder.client.MedicineInventoryClient;
import com.medicare.reminder.client.NotificationClient;
import com.medicare.reminder.dto.AdherenceStatsDto;
import com.medicare.reminder.dto.DoseLogResponse;
import com.medicare.reminder.entity.DoseLog;
import com.medicare.reminder.repository.DoseLogRepository;
import com.medicare.reminder.repository.MedicineScheduleRepository;
import com.medicare.reminder.service.impl.DoseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoseServiceTest {

    @Mock
    private DoseLogRepository doseLogRepository;

    @Mock
    private MedicineScheduleRepository scheduleRepository;

    @Mock
    private MedicineInventoryClient inventoryClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private DoseServiceImpl doseService;

    private DoseLog sampleDose;

    @BeforeEach
    void setUp() {
        sampleDose = DoseLog.builder()
                .id(100L)
                .scheduleId(1L)
                .patientId(3L)
                .medicineId(10L)
                .medicineName("Lisinopril 10mg")
                .scheduledTime(LocalDateTime.now().minusMinutes(10))
                .status(DoseStatus.SCHEDULED)
                .snoozeCount(0)
                .build();
    }

    @Test
    @DisplayName("Should successfully mark dose as TAKEN, record actual time, and trigger inventory deduction")
    void testTakeMedicineSuccess() {
        when(doseLogRepository.findByIdAndPatientId(100L, 3L)).thenReturn(Optional.of(sampleDose));
        when(doseLogRepository.save(any(DoseLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoseLogResponse response = doseService.takeMedicine(3L, 100L);

        assertNotNull(response);
        assertEquals(DoseStatus.TAKEN, response.getStatus());
        assertNotNull(response.getActualTime());

        // Verify inventory deduction feign call
        verify(inventoryClient, times(1)).deductStock(10L, 1);
    }

    @Test
    @DisplayName("Should snooze dose by shifting scheduled time by 15 minutes and incrementing snooze count")
    void testSnoozeMedicine() {
        when(doseLogRepository.findByIdAndPatientId(100L, 3L)).thenReturn(Optional.of(sampleDose));
        when(doseLogRepository.save(any(DoseLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DoseLogResponse response = doseService.snoozeMedicine(3L, 100L, 15);

        assertNotNull(response);
        assertEquals(DoseStatus.SNOOZED, response.getStatus());
        assertEquals(1, response.getSnoozeCount());
        assertTrue(response.getScheduledTime().isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Should accurately calculate adherence rate percentage")
    void testCalculateAdherence() {
        when(doseLogRepository.countTotalInRange(eq(3L), any(), any())).thenReturn(10L);
        when(doseLogRepository.countTakenInRange(eq(3L), any(), any())).thenReturn(8L);

        AdherenceStatsDto stats = doseService.calculateAdherence(3L, 30);

        assertNotNull(stats);
        assertEquals(10L, stats.getTotalScheduled());
        assertEquals(8L, stats.getTakenCount());
        assertEquals(new BigDecimal("80.0"), stats.getAdherencePercentage());
    }
}
