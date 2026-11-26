package com.medicare.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "report_title", nullable = false, length = 200)
    private String reportTitle;

    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType; // WEEKLY, MONTHLY, CUSTOM

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "adherence_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal adherenceRate = BigDecimal.ZERO;

    @Column(name = "vitals_summary_json", columnDefinition = "JSON")
    private String vitalsSummaryJson;

    @CreationTimestamp
    @Column(name = "generated_at", updatable = false)
    private LocalDateTime generatedAt;
}
