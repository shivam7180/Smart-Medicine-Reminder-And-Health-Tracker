package com.medicare.reminder.repository;

import com.medicare.reminder.entity.MedicineSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineScheduleRepository extends JpaRepository<MedicineSchedule, Long> {

    List<MedicineSchedule> findByPatientIdAndIsActiveTrue(Long patientId);

    Optional<MedicineSchedule> findByIdAndPatientId(Long id, Long patientId);

    List<MedicineSchedule> findByMedicineIdAndIsActiveTrue(Long medicineId);

    @Query("SELECT s FROM MedicineSchedule s WHERE s.isActive = true " +
           "AND s.startDate <= :targetDate " +
           "AND (s.endDate IS NULL OR s.endDate >= :targetDate)")
    List<MedicineSchedule> findActiveSchedulesForDate(@Param("targetDate") LocalDate targetDate);
}
