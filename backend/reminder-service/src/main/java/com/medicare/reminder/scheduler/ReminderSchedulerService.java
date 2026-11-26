package com.medicare.reminder.scheduler;

import com.medicare.common.enums.DoseStatus;
import com.medicare.reminder.client.NotificationClient;
import com.medicare.reminder.entity.DoseLog;
import com.medicare.reminder.repository.DoseLogRepository;
import com.medicare.reminder.service.DoseService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderSchedulerService {

    private final DoseService doseService;
    private final DoseLogRepository doseLogRepository;
    private final NotificationClient notificationClient;

    @Value("${reminder.grace-period-minutes:60}")
    private int gracePeriodMinutes;

    @PostConstruct
    public void init() {
        log.info("Reminder Engine initialized. Pre-populating today's doses...");
        doseService.generateDosesForDate(LocalDate.now());
    }

    /**
     * Daily job running at midnight (00:00:00) to pre-generate doses for all active patient schedules.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDailyDosesJob() {
        LocalDate today = LocalDate.now();
        log.info("Running daily scheduled dose generation for {}", today);
        doseService.generateDosesForDate(today);
    }

    /**
     * Periodic scanner running every minute to detect overdue unacknowledged doses and mark them as MISSED.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processMissedDosesJob() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(gracePeriodMinutes);
        List<DoseLog> overdueList = doseLogRepository.findOverdueScheduledDoses(DoseStatus.SCHEDULED, cutoff);

        for (DoseLog dose : overdueList) {
            dose.setStatus(DoseStatus.MISSED);
            dose.setSkipReason("Automatically marked as MISSED: Unacknowledged past " + gracePeriodMinutes + " minutes");
            doseLogRepository.save(dose);

            log.warn("Dose {} ({}) for patient {} automatically transitioned to MISSED",
                    dose.getId(), dose.getMedicineName(), dose.getPatientId());

            try {
                notificationClient.sendNotification(
                        dose.getPatientId(),
                        "Missed Dose Alert: " + dose.getMedicineName(),
                        "You missed your scheduled dose of " + dose.getMedicineName() + " scheduled for " + dose.getScheduledTime().toLocalTime() + ". Please consult your doctor if needed.",
                        "MISSED_DOSE",
                        dose.getId()
                );
            } catch (Exception e) {
                log.error("Failed to send missed dose notification: {}", e.getMessage());
            }
        }
    }
}
