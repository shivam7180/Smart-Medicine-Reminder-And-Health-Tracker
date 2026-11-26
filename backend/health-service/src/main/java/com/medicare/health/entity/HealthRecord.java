package com.medicare.health.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_records", indexes = {
        @Index(name = "idx_hr_patient_type", columnList = "patient_id, record_type"),
        @Index(name = "idx_hr_reading_time", columnList = "reading_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "record_type", nullable = false, length = 50)
    private String recordType; // BLOOD_PRESSURE, BLOOD_SUGAR, HEART_RATE, TEMPERATURE, WEIGHT, HEIGHT, BMI, SPO2, WATER_INTAKE, SLEEP, ACTIVITY

    @Column(name = "value_primary", nullable = false, precision = 10, scale = 2)
    private BigDecimal valuePrimary;

    @Column(name = "value_secondary", precision = 10, scale = 2)
    private BigDecimal valueSecondary; // e.g., Diastolic for Blood Pressure

    @Column(nullable = false, length = 30)
    private String unit;

    @Column(name = "reading_time", nullable = false)
    private LocalDateTime readingTime;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "abnormal_flag")
    private Boolean abnormalFlag = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
