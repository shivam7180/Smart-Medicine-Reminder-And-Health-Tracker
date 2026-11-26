package com.medicare.reminder.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminder_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReminderEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dose_log_id", nullable = false)
    private Long doseLogId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "reminder_time", nullable = false)
    private LocalDateTime reminderTime;

    @Column(length = 30)
    private String status; // SENT, PENDING, FAILED

    @CreationTimestamp
    @Column(name = "generated_at", updatable = false)
    private LocalDateTime generatedAt;
}
