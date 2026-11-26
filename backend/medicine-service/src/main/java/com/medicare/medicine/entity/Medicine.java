package com.medicare.medicine.entity;

import com.medicare.common.enums.MedicineType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private MedicineCategory category;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "generic_name", length = 150)
    private String genericName;

    @Enumerated(EnumType.STRING)
    @Column(name = "medicine_type", nullable = false, length = 50)
    private MedicineType medicineType;

    @Column(nullable = false, length = 50)
    private String strength; // e.g. "500 mg", "10 ml"

    @Column(name = "dosage_instructions", length = 255)
    private String dosageInstructions;

    @Column(name = "before_after_food", length = 30)
    private String beforeAfterFood; // BEFORE_FOOD, AFTER_FOOD, WITH_FOOD, ANYTIME

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "prescribing_doctor", length = 150)
    private String prescribingDoctor;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToOne(mappedBy = "medicine", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MedicineInventory inventory;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
