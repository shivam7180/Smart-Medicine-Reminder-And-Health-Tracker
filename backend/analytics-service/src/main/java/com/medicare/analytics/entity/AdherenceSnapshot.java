package com.medicare.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "adherence_snapshots", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"patient_id", "snapshot_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdherenceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "total_scheduled", nullable = false)
    @Builder.Default
    private Integer totalScheduled = 0;

    @Column(name = "taken_count", nullable = false)
    @Builder.Default
    private Integer takenCount = 0;

    @Column(name = "skipped_count", nullable = false)
    @Builder.Default
    private Integer skippedCount = 0;

    @Column(name = "missed_count", nullable = false)
    @Builder.Default
    private Integer missedCount = 0;

    @Column(name = "adherence_percentage", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal adherencePercentage = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
